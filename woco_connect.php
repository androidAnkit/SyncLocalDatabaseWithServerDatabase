<?php

$connection = func_mysql_connect();


$g8_total_string = '';
$name = '';
$str='';
$stor_result='';

## For fetching names
if(isset($_REQUEST['fetch']) && $_REQUEST['fetch']!=''){
	$data=fetch();
		if(@$data!=''){
			foreach($data as $data_val){
				if($stor_result==""){
					$stor_result=$data_val['name'];
				}else{
					$stor_result=$stor_result.'@!'.$data_val['name'];
				}
			}
			$res= '{"get_data": "'.$stor_result.'","status":"Success"}';
		}else{
			$res= '{"get_data": "0","status":"Failed"}';
		}
			$str.=$res;
		
}


// For inserting name
if(isset($_REQUEST['name']) && $_REQUEST['name'] != ''){
		$name = $_REQUEST['name'];
		$name=strtolower($name);
		$result = insert_name($name);
		if($result==1){
			$insert= '{"data_insert": "1","status":"Success"}';
		}else{
			$insert= '{"data_insert": "0","status":"Failed"}';
		}
		$str.=$insert;
}

// For deleting selected name
if(isset($_REQUEST['delete_name']) && $_REQUEST['delete_name'] != ''){
		$name = $_REQUEST['delete_name'];
		$name=strtolower($name);
		$result = delete_name($name);
		if($result==1){
			$delete= '{"data_deleted": "1","status":"Success"}';
		}else{
			$delete= '{"data_deleted": "0","status":"Failed"}';
		}
		$str.=$delete;
}		

if(isset($_REQUEST['check_name'])&& $_REQUEST['check_name']!=''){
	$check_name=$_REQUEST['check_name'];
	$check_name=strtolower($check_name);
	$data=fetch_name($check_name);
		if(@$data!='' && $data!='0'){
			foreach($data as $data_val){
				if($stor_result==""){
					$stor_result=$data_val['name'];
				}else{
					$stor_result=$stor_result.'@!'.$data_val['name'];
				}
			}
			$res= '{"get_data": "'.$stor_result.'","status":"Success"}';
		}else{
			$res= '{"get_data": "0","status":"Failed"}';
		}
			$str.=$res;
}	


function func_mysql_connect(){
	$server = "localhost";
	$user = "root";
	$password = "";
	$database_name  = "woco";

	$connection  = mysqli_connect($server, $user, $password, $database_name);

	if(mysqli_connect_errno()){
		printf("Connect failed: %s\n", mysqli_connect_error());
		exit;
	}else{
		$_SESSION['connection'] = $connection;
	}
}

function func_mysql_query($g8_sql){
	$result =$_SESSION['connection']->query($g8_sql);
	if(!$result){echo "Failed";}
	else{
		return $result;
		}
}

function func_mqsql_fetch_by_array_assoc($result){
	$row  = mysqli_fetch_array($result);
	return $row;
}

function func_mysql_escape_string($string){
	$result = mysqli_real_escape_string($_SESSION['connection'],$string);
	return $result;
}

##g8_create_table();


function g8_create_table(){
	$sql = "Create table IF NOT EXISTS `names`(rec_id int(6) AUTO_INCREMENT PRIMARY KEY , name varchar(40), g8_email text)";
	$result = func_mysql_query($sql);
	//~ echo "Table created";
}

function delete_name($name){
	$name = func_mysql_escape_string($name);
	$sql = "Delete from `names` Where `name`='".$name."'";
	//~ echo $sql;
	$result = func_mysql_query($sql);
	if($result>0){
		//~ echo "Data inserted";
		//~ g8_fetch_according_email($g8_email);
		return $result;
	}
}	
function insert_name($name){
	//~ $name=strtolower($name);
	$name = func_mysql_escape_string($name);
	$sql = "Insert into `names`(`name`) values('".$name."')";
	//~ echo $sql;
	$result = func_mysql_query($sql);
	if($result>0){
		//~ echo "Data inserted";
		//~ g8_fetch_according_email($g8_email);
		return $result;
	}
}

function fetch(){
	$sql = "Select `name` from `names`";
	//~ echo $sql;
	$result = func_mysql_query($sql);
	while (	$row = func_mqsql_fetch_by_array_assoc($result)){
		$column[]=$row; 
	}
	if(!isset($column)){
		$column='0';
	}
	return $column;
}

function fetch_name($name){
	$sql = "Select `name` from `names` where `name`='".$name."'";
	$result = func_mysql_query($sql);
	while (	$row = func_mqsql_fetch_by_array_assoc($result)){
		$column[]=$row; 
	}
	if(!isset($column)){
		$column='0';
	}
	return $column;
}



echo $str;

?>
