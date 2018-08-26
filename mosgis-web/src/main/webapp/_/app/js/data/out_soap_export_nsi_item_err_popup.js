define ([], function () {

    $_DO.open_out_soap_export_nsi_item_err_popup = function (e) {

        var col = this.columns [e.column]

        var attr = col.attr; if (!attr) return 

        openTab ('/' + attr.substr (9) + '/' + this.get (e.recid) [col.field])
    
    }

    return function (done) {

        var data = {dt: sessionStorage.getItem ('dt')}

        sessionStorage.removeItem ('dt')

        done (data)

    }

})