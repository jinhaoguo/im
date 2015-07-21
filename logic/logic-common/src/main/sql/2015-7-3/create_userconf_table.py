#! /bin/env python 
# -*- coding: utf-8 -*-
import MySQLdb
import MySQLdb.cursors
import time

#db config
HOST = '121.41.104.80'
USER = 'root'
PASSWD = 'Fs@123#@!'

IM_DB_NAME = "im"
 
exeFlag='true'
LogFileName="create_" + IM_DB_NAME + "_userconf_tables.sql"
db=MySQLdb.connect(host=HOST,user=USER,passwd=PASSWD,charset='utf8',cursorclass=MySQLdb.cursors.DictCursor)
cursor=db.cursor()
dbSize = 1
tbSize = 100
            
def createUserConfig():
    for dbFix in range(dbSize):
        tbname = "user_im_config"
        sql = "CREATE TABLE " + IM_DB_NAME + "." + tbname + '''(
                `userId` varchar(200) NOT NULL,
                `allowStrangerIm` int(11) DEFAULT '0',
                `createTime` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
                `updateTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                PRIMARY KEY (`userId`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8'''
        writeFile(sql, IM_DB_NAME, tbname)
            

def dropUserConfig():
    for dbFix in range(dbSize):
        for tbFix in range(tbSize):
            tbname = "user_msg_" + str(tbFix)
            sql = "DROP TABLE " + IM_DB_NAME + "." + tbname + ";"
            writeFile(sql, IM_DB_NAME, tbname)
            
def writeFile(sql, dbname, tbname):
    LogFile.write(sql + '; \n')
    if(exeFlag == 'true'):
        cursor.execute(sql)
        print "success to create table " + dbname + "." + tbname

def createDataBase(database):
    db=MySQLdb.connect(host=HOST,user=USER,passwd=PASSWD,charset='utf8',cursorclass=MySQLdb.cursors.DictCursor)
    cursor=db.cursor()
    sql = "CREATE DATABASE " + database
    LogFile.write(sql + '; \n')
    if(exeFlag == 'true'):
        cursor.execute(sql)
        print "success to create database " + database + " DEFAULT CHARSET=utf8mb4 "

def dropDataBase(database):
    db=MySQLdb.connect(host=HOST,user=USER,passwd=PASSWD,charset='utf8',cursorclass=MySQLdb.cursors.DictCursor)
    cursor=db.cursor()
    sql = "DROP DATABASE IF EXISTS " + database
    LogFile.write(sql + '; \n')
    if(exeFlag == 'true'):
        cursor.execute(sql)
        print "success to drop database " + database 
                
if __name__ == "__main__":
            
    LogFile = open(LogFileName,'w') 
    dropDataBase(IM_DB_NAME)
    createDataBase(IM_DB_NAME)
    createUserConfig()
    print "ok"