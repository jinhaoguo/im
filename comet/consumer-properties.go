package main
import (
	 "time"
     kafka "github.com/stealthly/go_kafka_client"
)
//DefaultConsumerConfig creates a ConsumerConfig with sane defaults. Note that several required config entries (like Strategy and callbacks) are still not set.
func DefaultConsumerConfig() *kafka.ConsumerConfig {
	config := &kafka.ConsumerConfig{}
	config.Groupid = "go-consumer-for-notify-queue-server1"
	config.SocketTimeout = 30 * time.Second
	config.FetchMessageMaxBytes = 1024 * 1024
	config.NumConsumerFetchers = 1
	config.QueuedMaxMessages = 3
	config.RebalanceMaxRetries = 4
	config.FetchMinBytes = 1
	config.FetchWaitMaxMs = 100
	config.RebalanceBackoff = 5 * time.Second
	config.RefreshLeaderBackoff = 200 * time.Millisecond
	config.OffsetsCommitMaxRetries = 5
	config.OffsetCommitInterval = 3 * time.Second

	config.AutoOffsetReset = kafka.LargestOffset
	config.Clientid = "go-client"
	config.ExcludeInternalTopics = true
	config.PartitionAssignmentStrategy = kafka.RangeStrategy /* select between "RangeStrategy", and "RoundRobinStrategy" */

	config.NumWorkers = 10
	config.MaxWorkerRetries = 3
	config.WorkerRetryThreshold = 100
	config.WorkerThresholdTimeWindow = 1 * time.Minute
	config.WorkerBackoff = 500 * time.Millisecond
	config.WorkerTaskTimeout = 1 * time.Minute
	config.WorkerManagersStopTimeout = 1 * time.Minute

	config.FetchBatchSize = 100
	config.FetchBatchTimeout = 10 * time.Millisecond

	config.FetchMaxRetries = 5
	config.RequeueAskNextBackoff = 5 * time.Second
	config.AskNextChannelSize = 1000
	config.FetchTopicMetadataRetries = 3
	config.FetchTopicMetadataBackoff = 1 * time.Second
	config.FetchRequestBackoff = 10 * time.Millisecond

	zkConfig:=kafka.NewZookeeperConfig()
	zkConfig.ZookeeperConnect=Conf.ZookeeperAddr
	config.Coordinator = kafka.NewZookeeperCoordinator(zkConfig)
	config.BlueGreenDeploymentEnabled = true
	config.DeploymentTimeout = 0 * time.Second
	config.BarrierTimeout = 30 * time.Second
	config.LowLevelClient = kafka.NewSaramaClient(config)

	config.KeyDecoder = &kafka.ByteDecoder{}
	config.ValueDecoder = config.KeyDecoder

	return config
}
