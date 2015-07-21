package main
import (
	gkc "github.com/stealthly/go_kafka_client"
	"encoding/json"
	log "github.com/alecthomas/log4go"
	"./controller"
)


func handleMessage(message string){
	msg:=&KafkaMsg{}
	log.Info("received message:%s",message)
	if err := json.Unmarshal([]byte(message), msg); err != nil {
		log.Warn("json.Unmarshal(\"%s\", rm) error(%v)", message, err)
		return
	}

	//1.check whether the userid's long connection on the instance.
	for _,uid:= range msg.Data.User_ids {
		userConn,err:=UserChannel.Get(uid,false)
		if (err!=nil || nil==userConn){
			log.Warn("user %s connection has been removed from the node %s, send logic offline msg", uid, Conf.NodeName)
			thriftClient, err := thriftPool.Get()
			if (err != nil) {
				log.Error("cannot get thrift client from pool, error(%v)", err)
			}

			thriftClient.(*controller.ControllerRpcServiceClient).NotifyConnectionStatus(Conf.NodeName,uid,0)
			thriftPool.Back(thriftClient)
			continue
		}
		//5. send to the user connection.
		str_msg,_:=json.Marshal(msg.Data.Notify)
		userConn.WriteMsg(uid, str_msg)
	}
}

//
func consumeMessage() {
	msgcount := make(chan int)
	config := DefaultConsumerConfig()
	//Sets strategy for dealing with messages. Adds messages as string to msgs channel
	config.Strategy = func(worker *gkc.Worker, msg *gkc.Message, taskId gkc.TaskId) gkc.WorkerResult {
		//handle the new arrived message here.
		//msgs <- string(msg.Value)
		handleMessage(string(msg.Value))
		msgcount <- 1
		return gkc.NewSuccessfulResult(taskId)
	}

	//Set failure callbacks
	config.WorkerFailureCallback = func(*gkc.WorkerManager) gkc.FailedDecision {
		return gkc.CommitOffsetAndContinue
	}
	config.WorkerFailedAttemptCallback = func(*gkc.Task, gkc.WorkerResult) gkc.FailedDecision {
		return gkc.CommitOffsetAndContinue
	}

	//Build new consumer from configuration
	consumer := gkc.NewConsumer(config)
	defer consumer.Close()
	//Start consuming for "testing" topic
	go consumer.StartStatic(map[string]int{
		Conf.ConsumerQueue: Conf.ConsumerQueuePartition,
	})

	//Print out messages from msg channel from "testing" topic
	var totalCount=0

	for {
			<-msgcount
			//fmt.Println(msg)
			totalCount+=1

	}

}
//func CreateKafkaTopic(zk) KafkaQueue


func createTopic(zookeeper string,topicName string,partitionCount int,sync2Controller bool){
	//CreateMultiplePartitionsTopic(zookeeper,topicName,partitionCount)
	//todo 需要同步消息到控制中心
}

func StartQueue() error {
	log.Info("begin to set the configuration for kafka queue")
	//gkc.CreateMultiplePartitionsTopic(Conf.ZookeeperAddr,Conf.ConsumerQueue,Conf.ConsumerQueuePartition)
	go consumeMessage()
	log.Info("end to set the configuration for kafka queue")

	return nil

}
