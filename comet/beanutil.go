package main
import (
	"fmt"
)

func SimpleMsgConvert(cmd string,data string)([]byte){
	return []byte(fmt.Sprintf("%s%s\r\n", cmd, data))
}
func JsonMsgConvert(data []byte)([]byte){
	return []byte(fmt.Sprintf("$%d\r\n%s\r\n", len(data), string(data)))
}