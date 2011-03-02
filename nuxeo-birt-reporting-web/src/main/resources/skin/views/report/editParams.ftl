<html>
<body>
  <form method="POST" action="${target}"/>
  <table>
    <#list params as param>
    <tr>
     <td>${param.displayName}
     </td>
     <td>
     <input name="${param.name}" value="${param.stringValue}">
     </td>
    </tr>
    </#list>
    <tr><td colspan="2">
    <input type="submit"/>
    </td></tr>
  </table>
  </form>
</body>
</html>