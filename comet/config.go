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
	"flag"
	"github.com/Terry-Mao/goconf"
	"runtime"
	"time"
	"net"
	"os"
	"fmt"
)

var (
	Conf     *Config
	confFile string
)

func init() {
	flag.StringVar(&confFile, "c", "./comet.conf", " set gopush-cluster comet config file path")
}

type Config struct {
	// base
	User          string   `goconf:"base:user"`
	PidFile       string   `goconf:"base:pidfile"`
	Dir           string   `goconf:"base:dir"`
	Log           string   `goconf:"base:log"`
	MaxProc       int      `goconf:"base:maxproc"`
	TCPBind       []string `goconf:"base:tcp.bind:,"`
	WebsocketBind []string `goconf:"base:websocket.bind:,"`
	RPCBind       []string `goconf:"base:rpc.bind:,"`
	PprofBind     []string `goconf:"base:pprof.bind:,"`
	StatBind      []string `goconf:"base:stat.bind:,"`
	// zookeeper
	ZookeeperAddr        []string      `goconf:"zookeeper:addr:,"`
	ZookeeperTimeout     time.Duration `goconf:"zookeeper:timeout:time"`
	ZookeeperCometPath   string        `goconf:"zookeeper:comet.path"`
	ZookeeperCometNode   string        `goconf:"zookeeper:comet.node"`
	ZookeeperCometWeight int           `goconf:"zookeeper:comet.weight"`
	ZookeeperMessagePath string        `goconf:"zookeeper:message.path"`
	// rpc
	RPCPing  time.Duration `goconf:"rpc:ping:time"`
	RPCRetry time.Duration `goconf:"rpc:retry:time"`
	// channel
	SndbufSize              int           `goconf:"channel:sndbuf.size:memory"`
	RcvbufSize              int           `goconf:"channel:rcvbuf.size:memory"`
	Proto                   []string      `goconf:"channel:proto:,"`
	BufioInstance           int           `goconf:"channel:bufio.instance"`
	BufioNum                int           `goconf:"channel:bufio.num"`
	TCPKeepalive            bool          `goconf:"channel:tcp.keepalive"`
	MaxSubscriberPerChannel int           `goconf:"channel:maxsubscriber"`
	ChannelBucket           int           `goconf:"channel:bucket"`
	Auth                    bool          `goconf:"channel:auth"`
	TokenExpire             time.Duration `goconf:"-"`
	MsgBufNum               int           `goconf:"channel:msgbuf.num"`
	ConsumerQueue			string      `goconf:"consumer:queue.name"`
	ConsumerQueuePartition			int      `goconf:"consumer:queue.partition"`
	NodeName				string		`goconf:"node:name"`
    ControllerServerIp  string `goconf:"controller:server.ip"`
	ControllerServerPort  string `goconf:"controller:server.port"`
	ControllerSyncInterval     int64   `goconf:"controller:sync.interval"`
	ThriftMaxActive     int   `goconf:"controller:thrift.max.active"`
	ThriftMaxIdle     int   `goconf:"controller:thrift.max.idle"`
	ThriftIdleTimeout     int64   `goconf:"controller:thrift.idle.timeout"`
}

// InitConfig get a new Config struct.
func InitConfig() error {
	Conf = &Config{
		// base
		User:          "nobody nobody",
		PidFile:       "/tmp/gopush-cluster-comet.pid",
		Dir:           "./",
		Log:           "./log/xml",
		MaxProc:       runtime.NumCPU(),
		WebsocketBind: []string{"localhost:6968"},
		TCPBind:       []string{"localhost:6969"},
		RPCBind:       []string{"localhost:6970"},
		PprofBind:     []string{"localhost:6971"},
		StatBind:      []string{"localhost:6972"},
		// zookeeper
		ZookeeperAddr:        []string{"120.26.78.119:2181"},
		ZookeeperTimeout:     30 * time.Second,
		ZookeeperCometPath:   "/gopush-cluster-comet",
		ZookeeperCometNode:   "node1",
		ZookeeperCometWeight: 1,
		ZookeeperMessagePath: "/gopush-cluster-message",
		// rpc
		RPCPing:  1 * time.Second,
		RPCRetry: 1 * time.Second,
		// channel
		SndbufSize:              2048,
		RcvbufSize:              256,
		Proto:                   []string{"tcp", "websocket"},
		BufioInstance:           runtime.NumCPU(),
		BufioNum:                128,
		TCPKeepalive:            false,
		TokenExpire:             30 * 24 * time.Hour,
		MaxSubscriberPerChannel: 64,
		ChannelBucket:           runtime.NumCPU(),
		Auth:                    false,
		MsgBufNum:               30,
	}
	//生成对应的消费的queue名字，node名字，
	interfaces,err:=net.Interfaces()
	if err!=nil{
		panic("Failed to get mac , here is what you got: " + err.Error())
	}
	mac:=""
	for _, inter := range interfaces {
		//fmt.Println(inter.Name)
		if inter.Name=="en0" {
			mac = inter.HardwareAddr.String() //获取本机MAC地址
		}

		log.Debug("MAC = %s", inter.HardwareAddr.String())
	}
	pid:=os.Getpid()
	log.Info("MAC = %s",mac)
	log.Info("pid = %d",pid)

	Conf.NodeName=fmt.Sprintf("%s-%d",mac,pid)
	Conf.ConsumerQueue=Conf.NodeName
	Conf.ConsumerQueuePartition=20
	log.Info("NodeName=%s,ConsumeQueue=%s",Conf.NodeName,Conf.ConsumerQueue)
	c := goconf.New()
	if err := c.Parse(confFile); err != nil {
		return err
	}
	if err := c.Unmarshal(Conf); err != nil {
		return err
	}
	log.Info("NodeName=%s,ConsumeQueue=%s",Conf.NodeName,Conf.ConsumerQueue)
	return nil
}
