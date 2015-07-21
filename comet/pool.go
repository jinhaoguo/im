package main

import (
	"sync"
	"time"
	"container/list"
	"errors"
	log "github.com/alecthomas/log4go"
)

var (
	ErrPoolClosed = errors.New("pool is closed")
	ErrPoolExhausted = errors.New("pool is exhausted")
)

type Pool struct {

	// Dial is an application supplied function for creating new connections.
	Dial func() (interface{}, error)

	// Close is an application supplied functoin for closeing connections.
	Close func(c interface{}) error

	// TestOnBorrow is an optional application supplied function for checking
	// the health of an idle connection before the connection is used again by
	// the application.  If the function returns an error, then the connection is
	// closed.
	TestOnBorrow func(c interface{}) error

	// Maximum number of idle connections in the pool.
	MaxIdle int

	// Maximum number of connections allocated by the pool at a given time.
	// When zero, there is no limit on the number of connections in the pool.
	MaxActive int

	// Close connections after remaining idle for this duration. If the value
	// is zero, then idle connections are not closed. Applications should set
	// the timeout to a value less than the server's timeout.
	IdleTimeout time.Duration

	// mu protects fields defined below.
	mu     sync.Mutex
	closed bool
	active int

	// Stack of idleConn with most recently used at the front.
	// get from back, and back to
	idle list.List
}

type idleConn struct {
	c interface{}
	t time.Time
}

// New creates a new pool. This function is deprecated. Applications should
// initialize the Pool fields directly as shown in example.
func New(dialFn func() (interface{}, error), closeFn func(c interface{}) error, maxIdle int) *Pool {
	return &Pool{Dial: dialFn, Close: closeFn, MaxIdle: maxIdle}
}

// Get gets a connection. The application must close the returned connection.
// This method always returns a valid connection so that applications can defer
// error handling to the first use of the connection.
func (p *Pool) Get() (interface{}, error) {
	if p.closed {
		return nil, ErrPoolClosed
	}

	log.Trace("")
	if timeout := p.IdleTimeout; timeout > 0 {
		log.Trace("check expire conn start")
		go func(){//check expire connection
			for e := p.idle.Front(); e != nil; e = e.Next(){
				ic := e.Value.(idleConn)
				if ic.t.Add(timeout).After(time.Now()) {
					p.mu.Lock()
					p.idle.Remove(e)
					p.active -= 1
					p.mu.Unlock()
					p.Close(ic.c)
				}
			}
		}()
	}

	log.Trace("Get idle connection")
	// Get idle connection.
	for e := p.idle.Back(); e != nil; e = e.Prev(){

		p.mu.Lock()
		c := p.idle.Remove(e).(idleConn).c
		p.mu.Unlock()
		log.Trace("TestOnBorrow before return old thrift client.")
		if(p.TestOnBorrow(c) != nil){
			p.active -= 1
			p.Close(c)
			continue
		} else {
			log.Trace("return old thrift client.")
			return c, nil
		}
	}

	log.Trace("exhaust if active count more than MaxActive")
	// No idle connection, pool exhaust if active count more than MaxActive
	if p.active >= p.MaxActive{
		p.mu.Unlock()
		return nil, ErrPoolExhausted
	}

	log.Trace("No idle connection, create new.")
	// No idle connection, create new.
	dial := p.Dial
	p.active += 1
	c, err := dial()
	if err != nil {
		p.mu.Lock()
		p.active -= 1
		p.mu.Unlock()
		c = nil
	}

	log.Trace("return new thrift client.")
	return c, err
}

// back conn to the pool after it is used over
func (p *Pool) Back(c interface{}) error {
	var err error
	if !p.closed {
		p.mu.Lock()
		p.idle.PushFront(idleConn{t: time.Now(), c: c})

		if p.idle.Len() > p.MaxIdle {// remove exceed conn, hardly happen if maxactive equals maxidle
			c = p.idle.Remove(p.idle.Back()).(idleConn).c
			if(c != nil){
				p.active -= 1
				err = p.Close(c)
			}
		}
		p.mu.Unlock()
	}

	return err
}

// Relaase releases the resources used by the pool.
func (p *Pool) Release() error {
	p.mu.Lock()
	idle := p.idle
	p.idle.Init()
	p.closed = true
	p.active -= idle.Len()
	p.mu.Unlock()
	for e := idle.Front(); e != nil; e = e.Next() {
		p.Close(e.Value.(idleConn).c)
	}
	return nil
}
