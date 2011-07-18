<head>
<title>Login</title>
<style type='text/css' media='screen'>
body {
	background: #202020;
	font-family: Arial;
}

div.openid-loginbox {
	width: 450px;
	margin-left: auto;
	margin-right: auto;
	background: white;
	padding: 15px;
}

.openid-loginbox-inner {
	width: 450px;
	border: 1px blue solid;
}

td.openid-loginbox-title {
	background: #e0e0ff;
	border-bottom: 1px #c0c0ff solid;
	padding: 0;
}

td.openid-loginbox-title table {
	width: 100%;
	font-size: 18px;
}
.openid-loginbox-useopenid {
	font-weight: normal;
	font-size: 14px;
}
td.openid-loginbox-title img {
	border: 0;
	vertical-align: middle;
	padding-right: 3px;
}
table.openid-loginbox-userpass {
	margin: 3px 3px 3px 8px;
}
table.openid-loginbox-userpass td {
	height: 25px;
}
input.openid-identifier {
	background: url(http://stat.livejournal.com/img/openid-inputicon.gif) no-repeat;
	background-color: #fff;
	background-position: 0 50%;
	padding-left: 18px;
}

input[type='text'],input[type='password'] {
	font-size: 16px;
	width: 310px;
}
input[type='submit'] {
	font-size: 14px;
}

td.openid-submit {
	padding: 3px;
}

</style>
</head>

<body>

<div class="openid-loginbox">

	<g:if test='${flash.message}'>
	<div class='login_message'>${flash.message}</div>
	</g:if>

	<table class='openid-loginbox-inner' cellpadding="0" cellspacing="0">
		<tr>
			<td class="openid-loginbox-title">
				<table>
					<tr>
						<td align="left">Please log in:</td>
						<td align="right" class="openid-loginbox-useopenid">
							<input type="checkbox" id="toggle" checked='checked' onclick='toggleForms()'/>
							<label for='toggle'>Use OpenID</label>
						</td>
					</tr>
				</table>
			</td>
		</tr>
		<tr>
			<td>

			<div id='openidLogin'>
				<form action='${openIdPostUrl}' method='POST' autocomplete='off' name='openIdLoginForm'>
				<table class="openid-loginbox-userpass">
					<tr>
						<td>OpenID:</td>
						<td><input type="text" name="${openidIdentifier}" class="openid-identifier"/></td>
					</tr>
					<g:if test='${persistentRememberMe}'>
					<tr>
						<td><label for='remember_me'>Remember me</label></td>
						<td>
							<input type='checkbox' name='${rememberMeParameter}' id='remember_me'/>
						</td>
					</tr>
					</g:if>
					<tr>
						<td colspan='2' class="openid-submit" align="center">
							<input type="submit" value="Log in" />
						</td>
					</tr>
				</table>
				</form>
			</div>

			<div id='formLogin' style='display: none'>
				<form action='${daoPostUrl}' method='POST' autocomplete='off'>
				<table class="openid-loginbox-userpass">
					<tr>
						<td>Username:</td>
						<td><input type="text" name='j_username' id='username' /></td>
					</tr>
					<tr>
						<td>Password:</td>
						<td><input type="password" name='j_password' id='password' /></td>
					</tr>
					<tr>
						<td><label for='remember_me'>Remember me</label></td>
						<td>
							<input type='checkbox' name='${rememberMeParameter}' id='remember_me'/>
						</td>
					</tr>
					<tr>
						<td colspan='2' class="openid-submit" align="center">
							<input type="submit" value="Log in" />
						</td>
					</tr>
				</table>
				</form>
			</div>

			</td>
		</tr>
	</table>
</div>

<script>

(function() { document.forms['openIdLoginForm'].elements['openid_identifier'].focus(); })();

var openid = true;

function toggleForms() {
	if (openid) {
		document.getElementById('openidLogin').style.display = 'none';
		document.getElementById('formLogin').style.display = '';
	}
	else {
		document.getElementById('openidLogin').style.display = '';
		document.getElementById('formLogin').style.display = 'none';
	}
	openid = !openid;
}
</script>
</body>
