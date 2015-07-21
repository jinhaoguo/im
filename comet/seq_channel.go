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
	"errors"
	"github.com/Terry-Mao/gopush-cluster/hlist"
	"encoding/json"
	//myrpc "github.com/Terry-Mao/gopush-cluster/rpc"
	"sync"
	"./controller"
)

var (
	//ErrMessageSave   = errors.New("Message set failed")
	//ErrMessageGet    = errors.New("Message get failed")
	//ErrMessageRPC    = errors.New("Message RPC not init")
	ErrAssectionConn = errors.New("Assection type Connection failed")

	DirectOfflineNotifyStr,_ = json.Marshal(&DirectOfflineBean{NotifyType:100, Show:"账号在别处登录，\r\n您被迫下线"})
)

// Sequence Channel struct.
type SeqChannel struct {
	//conn []Connection
	// Mutex
	mutex *sync.Mutex//需要么？一个用户的多个终端，应该不需要锁吧？还是需要,因为回调是多线程
	// client conn double linked-list 为什么呢？可能一个用户有多个终端，需要同步消息
	conn *hlist.Hlist
	// Remove time id or lazy New
	// timeID *id.TimeID
	// token
	//token *Token//需要么？接入前应该验证了token了，应该不需要了吧。
}

// New a user seq stored message channel.
func NewSeqChannel() *SeqChannel {
	ch := &SeqChannel{
		mutex: &sync.Mutex{},
		conn:  hlist.New(),
		//timeID: id.NewTimeID(),
		//token: nil,
	}
	// save memory
	//if Conf.Auth {
	//	ch.token = NewToken()
	//}
	return ch
}

// AddToken implements the Channel AddToken method.
func (c *SeqChannel) AddToken(key, token string) error {
//	if !Conf.Auth {
//		return nil
//	}
//	c.mutex.Lock()
//	if err := c.token.Add(token); err != nil {
//		c.mutex.Unlock()
//		log.Error("user_key:\"%s\" c.token.Add(\"%s\") error(%v)", key, token, err)
//		return err
//	}
//	c.mutex.Unlock()
	return nil
}

// AuthToken implements the Channel AuthToken method.
func (c *SeqChannel) AuthToken(key, token string) bool {
//	if !Conf.Auth {
//		return true
//	}
//	c.mutex.Lock()
//	if err := c.token.Auth(token); err != nil {
//		c.mutex.Unlock()
//		log.Error("user_key:\"%s\" c.token.Auth(\"%s\") error(%v)", key, token, err)
//		return false
//	}
//	c.mutex.Unlock()
	return true
}

// WriteMsg implements the Channel WriteMsg method.
func (c *SeqChannel) WriteMsg(key string, m json.RawMessage ) (err error) {
	c.mutex.Lock()
	err = c.writeMsg(key, m)
	c.mutex.Unlock()
	return
}

// writeMsg write msg to conn.
func (c *SeqChannel) writeMsg(key string, m json.RawMessage) (err error) {
	// push message
	for e := c.conn.Front(); e != nil; e = e.Next() {
		conn, _ := e.Value.(*Connection)
		conn.WriteNotify(key, m)
	}
	return
}

// PushMsg implements the Channel PushMsg method.
func (c *SeqChannel) PushMsg(key string,m json.RawMessage, expire uint) (err error) {
//	client := myrpc.MessageRPC.Get()
//	if client == nil {
//		return ErrMessageRPC
//	}
//	c.mutex.Lock()
//	// private message need persistence
//	// if message expired no need persistence, only send online message
//	// rewrite message id
//	//m.MsgId = c.timeID.ID()
//	m.MsgId = id.Get()
//	if m.GroupId != myrpc.PublicGroupId && expire > 0 {
//		args := &myrpc.MessageSavePrivateArgs{Key: key, Msg: m.Msg, MsgId: m.MsgId, Expire: expire}
//		ret := 0
//		if err = client.Call(myrpc.MessageServiceSavePrivate, args, &ret); err != nil {
//			c.mutex.Unlock()
//			log.Error("%s(\"%s\", \"%v\", &ret) error(%v)", myrpc.MessageServiceSavePrivate, key, args, err)
//			return
//		}
//	}
//	// push message
//	if err = c.writeMsg(key, m); err != nil {
//		c.mutex.Unlock()
//		log.Error("c.WriteMsg(\"%s\", m) error(%v)", key, err)
//		return
//	}
//	c.mutex.Unlock()
	return
}

// AddConn implements the Channel AddConn method.
// implement sso
func (c *SeqChannel) AddConn(key string, conn *Connection) (*hlist.Element, error) {
	log.Debug("user_key:%s from %s add conn start, count= %d", key, conn.Conn.RemoteAddr().String(), c.conn.Len())
	c.mutex.Lock()
	if c.conn.Len()+1 > Conf.MaxSubscriberPerChannel {
		c.mutex.Unlock()
		log.Error("user_key:\"%s\" exceed conn", key)
		return nil, ErrMaxConn
	}

	log.Trace("user_key:%s from %s add conn send hearbeat, count= %d", key, conn.Conn.RemoteAddr().String(), c.conn.Len())

	// send first heartbeat to tell client service is ready for accept heartbeat
	if _, err := conn.Conn.Write(HeartbeatReply); err != nil {
		c.mutex.Unlock()
		log.Error("user_key:\"%s\" write first heartbeat to client error(%v)", key, err)
		return nil, err
	}

	log.Trace("user_key:%s from %s add conn remove others, count= %d", key, conn.Conn.RemoteAddr().String(), c.conn.Len())
	// remove all other connection for sso
	for e := c.conn.Front(); e != nil; e = e.Next() {
		if oldConn, ok := e.Value.(*Connection); !ok {
			c.mutex.Unlock()
			return nil,ErrAssectionConn
		} else {
			log.Info("user %s login from %s, close other connection of this user, send chan, connip:%s",
				key, conn.Conn.RemoteAddr().String(), oldConn.Conn.RemoteAddr().String())
			oldConn.WriteNotify(key, DirectOfflineNotifyStr)

			// must clear chan, 如果前端没有处理offline消息，导致整个buf里面有内容没有被消费，
			// 会在下一条中阻塞。导致所有新的链接都阻塞在下面一句。
			// select能保证当不阻塞的时候，才往里面填, 当阻塞的时候，也执行第二个。保证了不会在填buf时阻塞
			select {
				case <-oldConn.AppLogicBuf:// 如果AppLogicBuf为空， 也会阻塞。
					oldConn.AppLogicBuf<-2
				case oldConn.AppLogicBuf<-2:
			}
		}
	}

	log.Trace("user_key:%s from %s add sso over, count= %d", key, conn.Conn.RemoteAddr().String(), c.conn.Len())

	// add conn
	conn.AppLogicBuf = make(chan int, 1)
	conn.WriteBuf = make(chan int, 1)

	//conn.HandleWrite(key)
	e := c.conn.PushFront(conn)
	c.mutex.Unlock()
	ConnStat.IncrAdd()
	//通知controller 用户上线
	log.Info("add conn, and then notify user %s online status:1, ip:%s, node:%s, count=%d", key, conn.Conn.RemoteAddr().String(), Conf.NodeName, c.conn.Len())
	thriftClient, err := thriftPool.Get()
	if (err != nil) {
		log.Error("cannot get thrift client from pool, error(%v)", err)
	}

	thriftClient.(*controller.ControllerRpcServiceClient).NotifyConnectionStatus(Conf.NodeName,key,1)
	thriftPool.Back(thriftClient)

	return e, nil
}

// RemoveConn implements the Channel RemoveConn method.
func (c *SeqChannel) RemoveConn(key string, e *hlist.Element) error {
	log.Debug("user %s remove from ip %s start, connetion count: %d", key, e.Value.(*Connection).Conn.RemoteAddr().String(), c.conn.Len());
	c.mutex.Lock()
	tmp := c.conn.Remove(e)
	c.mutex.Unlock()

	conn, ok := tmp.(*Connection)
	if !ok {
		return ErrAssectionConn
	}

	ip := conn.Conn.RemoteAddr().String()
	close(conn.WriteBuf)
	close(conn.AppLogicBuf)
	ConnStat.IncrRemove()

	log.Info("user %s offline from ip %s, maybe notify logic service, connetion count: %d", key, ip, c.conn.Len());
	if(c.conn.Len() == 0){// 如果一个用户所有的connection都不在线了，才通知logic下线。
		log.Info("remove all conn, and then notify user %s online status:0, ip:%s, node:%s, count=%d", key, ip, Conf.NodeName, c.conn.Len())
		thriftClient, err := thriftPool.Get()
		if (err != nil) {
			log.Error("cannot get thrift client from pool, error(%v)", err)
		}

		thriftClient.(*controller.ControllerRpcServiceClient).NotifyConnectionStatus(Conf.NodeName,key,0)
		thriftPool.Back(thriftClient)

	}

	return nil
}

// Close implements the Channel Close method.
func (c *SeqChannel) Close() error {
	c.mutex.Lock()
	for e := c.conn.Front(); e != nil; e = e.Next() {
		if conn, ok := e.Value.(*Connection); !ok {
			c.mutex.Unlock()
			return ErrAssectionConn
		} else {
			if err := conn.Conn.Close(); err != nil {
				// ignore close error
				log.Warn("conn.Close() error(%v)", err)
			}
		}
	}
	c.mutex.Unlock()
	return nil
}
