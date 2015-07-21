package main
//import "encoding/json"

// message entity to notify the user
//type MessageBean struct {
//	type int
//	notify string
//}

type KafkaMsg struct {
	Cname string  `json:"Cname"`
	Data PushBean  `json:"data"`
}
type PushBean struct {
	User_ids[] string  `json:"user_ids"`
	Notify map[string]interface{}  `json:"notify"`
}

type DirectOfflineBean struct {
	NotifyType int `json:"notify_type"`	//为100表示强制下线
	Show string  `json:"show"`	//下线前端显示信息
}

type ConnectionData struct {
	Is_token int
	Is_web_socket int
	User_token string
	Mac_token string
	Mac string
	Userid string
	Sign string
	Appplt string
	Appversion string
	Protoversion string
	Heart int
}

type ErrorMessageData struct {
	Er int `json:"er"`
	Desc string `json:"desc"`
	Ext string `json:"ext"`
}

//add another type of push message here.
