# secretsocks
<p>本系统主要的目的在于，在两台主机之间通过Socks5协议构建一个安全可靠、具有伪装机制的连接。
在公司中信息安全是第一要素，因为新员工的身份HR很难完全掌握，万一其是其他公司派来的二五仔，则一些机密的通讯很可能会被侦听，也有可能对通讯工具进行攻击。
因此，若将通讯工具的信道进行一定的伪装，则会很大程度上干扰二五仔的工作，并且若能提供类似于数据防火墙的功能，也是很吼的。</p>

说明
<ul>
<li>详细情况请见<a href="https://github.com/qwesdfok/secretsocks/wiki">WiKi</a></li>
<li>常见问题见<a href="https://github.com/qwesdfok/secretsocks/wiki/Q&A">Q&amp;A</a></li>
<li>库文件请根据client和server的依赖关系，放到与之对应的相同目录下。下载地址：<a href="https://github.com/qwesdfok/secretsocks/releases/download/1.0.0/jdom.jar">jdom.jar</a>，<a href="https://github.com/qwesdfok/secretsocks/releases/download/1.0.0/fluent-http.jar">fluent-http</a></li>
</ul>
<div>所需的其他库<div>
<ul>
<li><a href="http://www.jdom.org/">JDom</a>(仅客户端需要)</li>
<li><a href="https://github.com/CodeStory/fluent-http">fluent-http</a>(仅服务器需要，也可<a href="https://github.com/qwesdfok/secretsocks/wiki/Q&A">删除</a>)</li>
<li><a href="http://testng.org/doc/">testNG</a>(仅测试工程需要)</li>
</ul>