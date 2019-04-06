
define ([], function () {
    
    return function (data, view) {           
        
        fill (view, data, $('body'))
        
        clickOn ($('#logout'), $_DO.logout_page)
        
        clickOn ($('tr.nav td:first-child'), function () {
            openTab ('/', '/mos/')
        })

        if ($_USER.uuid_org) clickOn ($('span#org_label'), function () {
            openTab ('/voc_organization_legal/' + $_USER.uuid_org)
        })
        
        if ($_REQUEST.type) {
            use.block ($_REQUEST.type)
        }
        else {
            window.name = '/mosgis/'
            use.block ('main')        
        }
        

    }

});