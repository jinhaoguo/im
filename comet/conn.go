// Copyright © 2014 Terry Mao, LiuDing All rights reserved.
// This file is part of gopush-cluster.

// gopush-cluster is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// gopush-cluster is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with gopush-cluster.  If not, see <http://www.gnu.org/licenses/>.

package main

import (
	log "github.com/alecthomas/log4go"
	"net"
)

// Connection
type Connection struct {
	Conn    net.Conn
	Proto   uint8
	Version string
	WriteBuf     chan int//用来锁住conn，多线程写有序
	AppLogicBuf chan int//用来控制和app client交互的逻辑。2 表示强制app下线
}

// HandleWrite start a goroutine get msg from chan, then send to the conn.目前暂时不需要这个函数.
func (c *Connection) HandleWrite(key string) {
	go func() {
		var (
			n   int
			err error
		)
		log.Debug("user_key: \"%s\" HandleWrite goroutine start", key)
		for {
			msg, ok := <-c.WriteBuf
			if !ok {
				log.Debug("user_key: \"%s\" HandleWrite goroutine stop", key)
				return
			}
			if c.Proto == WebsocketProto {
				// raw
				//n, err = c.Conn.Write(msg)
			} else if c.Proto == TCPProto {
				// redis protocol
				//ouput := []byte(fmt.Sprintf("$%d\r\n%s\r\n", len(msg), string(msg)))
				//n, err = c.Conn.Write(ouput)
			} else {
				log.Error("unknown connection protocol: %d", c.Proto)
				panic(ErrConnProto)
			}
			// update stat
			if err != nil {
				log.Error("user_key: \"%s\" conn.Write() error(%v)", key, err)
				MsgStat.IncrFailed(1)
			} else {
				log.Debug("user_key: \"%s\" write \r\n========%s(%d)========", key, string(msg), n)
				MsgStat.IncrSucceed(1)
			}
		}
	}()
}

// Write different message to client by different protocol
//func (c *Connection) Write(key string, msg []byte) {
//	go c.WriteDirect(key, msg)
////			select {
////			case c.Buf <- msg:
////			default:
////				c.Conn.Close()
////				log.Warn("user_key: \"%s\" discard message: \"%s\" and close connection", key, string(msg))
////			}
//}

// Write different message to client by different protocol
func (c *Connection) WriteNotify(key string, msg []byte) {
	var (
		n int
		err error
	)
	notify:=JsonMsgConvert(msg)

	flag := 1
	for flag == 1{
		select {

			case c.WriteBuf<-1:
				{
					if c.Proto == WebsocketProto {
						// raw
						n, err = c.Conn.Write(notify)
					} else if c.Proto == TCPProto {
						// redis protocol
						//msg = []byte(fmt.Sprintf("$%d\r\n%s\r\n", len(msg), string(msg)))
						log.Debug("send client %s start, ip:%s, msg:%s", key, c.Conn.RemoteAddr().String(), string(notify))
						n, err = c.Conn.Write(notify)
					} else {
						log.Error("unknown connection protocol: %d", c.Proto)
						panic(ErrConnProto)
					}
					// update stat
					if err != nil {
						log.Debug("send client %s error, ip:%s, msg:%s", key, c.Conn.RemoteAddr().String(), string(notify))
						MsgStat.IncrFailed(1)
					} else {
						log.Debug("send client %s ok, ip:%s, msg:%s,%d", key, c.Conn.RemoteAddr().String(), string(notify), n)
						MsgStat.IncrSucceed(1)
					}
					<-c.WriteBuf
					flag = 0
				}

		}
	}

}

func (c *Connection) WriteComand(key string, data string) {
	var (
		n int
		err error
	)
	command := SimpleMsgConvert("+", data)

	flag := 1
	for flag == 1{
		select {

			case c.WriteBuf<-1:{
				if c.Proto == WebsocketProto {
					// raw
					n, err = c.Conn.Write(command)
				} else if c.Proto == TCPProto {
					// redis protocol
					log.Debug("send client %s start, ip:%s, msg:%s", key, c.Conn.RemoteAddr().String(), string(command))
					n, err = c.Conn.Write(command)
				} else {
					log.Error("unknown connection protocol: %d", c.Proto)
					panic(ErrConnProto)
				}
				// update stat
				if err != nil {
					log.Debug("send client %s error, ip:%s, msg:%s,", key, c.Conn.RemoteAddr().String(), string(command))
					MsgStat.IncrFailed(1)
				} else {
					log.Debug("send client %s ok, ip:%s, msg:%s,%d", key, c.Conn.RemoteAddr().String(), string(command),n)
					MsgStat.IncrSucceed(1)
				}
				<-c.WriteBuf

				flag = 0
			}
		}
	}

}
