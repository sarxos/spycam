<?php

include_once('config.php');


if (!isset($_FILES['picture'])) {
	die('No picture');
}

if (!isset($_POST['passwd'])) {
	die('Wrong password');
} else {
	if ($_POST['passwd'] !== $SPY_CONFIG['passwd']) {
		die('Incorrect password');
	}
}




$dir = $SPY_CONFIG['dir'];
if (substr($dir, strlen($dir) - 1, 1) !== '/') {
	$dir = $dir . '/';
}

$file = $_FILES['picture'];
$dst_name = time() . '.jpg';
$tmp_name = $file["tmp_name"];

move_uploaded_file($tmp_name, $dir . $dst_name);

print($dst_name);
