<html>
  <head>
  <script type="text/javascript" src="${skinPath}/script/jquery/jquery.js"></script>
  <link rel="stylesheet" href="${skinPath}/css/birt.css" type="text/css" media="screen" charset="utf-8">
  <script>
    function switchTarget(value) {
     var format = jQuery(value).val();
     jQuery("#birtForm").attr("action", format);
    }
  </script>
  </head>
  <body>
    <p class="specification">${Context.getMessage('label.birt.introParagraph')}</p>
    <table class="dataTableNoborder">
      <tr>
        <td class="bigCell">
          ${Context.getMessage('label.birt.reportFormat')}
        </td>
        <td>
        <select onchange="switchTarget(this)">
          <option value="html" <#if target == "html">selected</#if>>HTML</option>
          <option value="pdf" <#if target == "pdf">selected</#if>>PDF (Print)</option>
        </select>
        </td>
      </tr>
      <tr>
        <td class="bigCell">
        ${Context.getMessage('label.birt.reportParameters')}
        </td>
        <td>
          <form method="POST" id="birtForm" action="${target}"/>
            <table>
              <#list params as param>
              <tr>
               <td class="paramCell<#if param.error> inputErrorlabel</#if>">
                ${param.displayName}
               </td>
               <td>
                <input name="${param.name}" value="${param.stringValue}"
                  <#if param.error> class="inputError"</#if>>
               </td>
               <td class="paramHint">
                  (${param.typeName}
                  <#if param.typeFormat??>
                   [ ${param.typeFormat} ]
                  </#if>)
               </td>
              </tr>
              </#list>
              <tr>
                <td>
                  <input class="button" type="submit" value="${Context.getMessage('label.birt.generateReport')}"/>
                </td>
                <td colspan="2">
                  <a class="link" href="${This.path}/clearParams?target=${target}">${Context.getMessage('label.birt.clearParameters')}</a>
                </td>
              </tr>
            </table>
          </form>
        </td>
      </tr>
    </table>
  </body>
</html>
