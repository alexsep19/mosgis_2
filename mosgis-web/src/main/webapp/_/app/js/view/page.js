
define ([], function () {
    
    return function (data, view) {           
        
        fill (view, data, $('body'))
        
        clickOn ($('#logout'), $_DO.logout_page)
        
        if ($_USER.uuid_org) clickOn ($('span#org_label'), function () {
            openTab ('/voc_organization_legal/' + $_USER.uuid_org)
        })
        
        use.block ($_REQUEST.type || 'main')        
    
    }

});