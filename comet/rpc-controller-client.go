package main

import (
	"errors"
	"./controller"
	"git.apache.org/thrift.git/lib/go/thrift"
	"net"
	log "github.com/alecthomas/log4go"
	"time"
)

var (
	thriftPool *Pool
	ErrConnectionClosed = errors.New("conn is closed")
)

func InitControllerRpcClient() error {
	//	//startTime := currentTimeMillis()
	//	transportFactory := thrift.NewTFramedTransportFactory(thrift.NewTTransportFactory())
	//	protocolFactory := thrift.NewTBinaryProtocolFactoryDefault()
	//
	//	transport, err := thrift.NewTSocket(net.JoinHostPort(Conf.ControllerServerIp, Conf.ControllerServerPort))
	//	if err != nil {
	//		fmt.Fprintln(os.Stderr, "error resolving address:", err)
	//		return nil
	//	}
	//
	//	useTransport := transportFactory.GetTransport(transport)
	//	controllerClient = controller.NewControllerRpcServiceClientFactory(useTransport, protocolFactory)
	//	if err := useTransport.Open(); err != nil {
	//		fmt.Fprintln(os.Stderr, "Error opening socket to "+Conf.ControllerServerIp+":"+Conf.ControllerServerPort, " ", err)
	//		return err
	//	}
	//	//defer transport.Close()
	//	return nil

	thriftPool = &Pool{
		Dial: func() (interface{}, error) {
			transportFactory := thrift.NewTFramedTransportFactory(thrift.NewTTransportFactory())
			protocolFactory := thrift.NewTBinaryProtocolFactoryDefault()

			transport, err := thrift.NewTSocket(net.JoinHostPort(Conf.ControllerServerIp, Conf.ControllerServerPort))
			if err != nil {
				log.Error("error resolving address: %s, port: %s error(%v)", Conf.ControllerServerIp,
					Conf.ControllerServerPort, err)
				return nil, err
			}

			useTransport := transportFactory.GetTransport(transport)
			client := controller.NewControllerRpcServiceClientFactory(useTransport, protocolFactory)
			if err = client.Transport.Open(); err != nil {
				log.Error("client.Transport.Open() error(%v)", err)
				return nil, err
			}
			return client, nil
		},
		Close: func(v interface{}) error {
			v.(*controller.ControllerRpcServiceClient).Transport.Close()
			return nil
		},
		TestOnBorrow:func(v interface{}) error {
			if v.(*controller.ControllerRpcServiceClient).Transport.IsOpen(){
				return nil
			} else {
				return ErrConnectionClosed
			}
		},
		MaxActive:   Conf.ThriftMaxActive,
		MaxIdle:     Conf.ThriftMaxIdle,
		IdleTimeout: time.Duration(Conf.ThriftIdleTimeout),
	}

	return  nil;
}

