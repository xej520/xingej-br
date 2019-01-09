
#!/bin/bash
echo "---start checking mysql service-------"
while true
do
    res=`netstat -anlp|grep 3306`

    echo "-----"
    echo $res
    if [[ "a"$res != "a" ]];then
        echo "connect to MySQL server OK!"


        break
    fi

done

echo "---start bonc-broker service-------"

java -jar bonc-broker.jar

