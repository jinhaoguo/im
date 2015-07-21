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
	"github.com/Terry-Mao/gopush-cluster/perf"
//	"../process"
	"github.com/Terry-Mao/gopush-cluster/ver"
	"runtime"
)

func main() {
	// parse cmd-line arguments
	flag.Parse()
	log.Info("comet ver: \"%s\" start", ver.Version)
	// init config
	if err := InitConfig(); err != nil {
		panic(err)
	}
	// set max routine
	runtime.GOMAXPROCS(Conf.MaxProc)
	// init log
	log.Info("comet log configuration")
	log.LoadConfiguration(Conf.Log)
	defer log.Close()
	// start pprof
	log.Info("comet init perf")
	perf.Init(Conf.PprofBind)
	// create channel
	// if process exit, close channel
	UserChannel = NewChannelList()
	defer UserChannel.Close()
	// start stats
	log.Info("comet start stats")

	StartStats()
	// start rpc,应该不需要了吧？
	log.Info("comet start rpc")

	if err := InitControllerRpcClient(); err != nil {
		panic(err)
	}
	defer thriftPool.Release()

	// start comet
	log.Info("comet start ")
	if err := StartComet(); err != nil {
		panic(err)
	}

	// configuration the kafka topic and kafka consumer to consum the notify messages.
	log.Info("queue start")
	if err := StartQueue(); err != nil {
		panic(err)
	}

	// init zookeeper，用来监控instance的启动和服务down。同步instance的ip，queue，信息到配置中心。配置中心监控监控变动，同步信息到调度中心。
	zkConn, err := InitZK()
	if err != nil {
		if zkConn != nil {
			zkConn.Close()
		}
		panic(err)
	}
	// process init
//	if err = process.Init(Conf.User, Conf.Dir, Conf.PidFile); err != nil {
//		panic(err)
//	}
	// init signals, block wait signals
	signalCH := InitSignal()
	HandleSignal(signalCH)
	// exit
	log.Info("comet stop")
}
