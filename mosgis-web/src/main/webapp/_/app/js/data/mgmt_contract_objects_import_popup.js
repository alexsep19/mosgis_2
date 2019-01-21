define ([], function () {

    $_DO.update_mgmt_contract_objects_import_popup = function (e) {
/*
        var f = w2ui ['mgmt_contract_objects_import_popup_form']

        var v = f.values ()
        
        var file = get_valid_gis_file (v, 'files')
                                                  
        Base64file.upload (file, {
        
            type: 'charter_payment_docs',

            data: {
                uuid_charter_payment: $_REQUEST.id,
                description: v.description,
            },
            
            onprogress: show_popup_progress (file.size),
            
            onloadend: function (id) {
                w2popup.close ()
                var form = w2ui ['charter_payment_common_form']
                var fn = $_SESSION.delete ('field_name')
                var o = {id: id, text: file.name}
                form.get ('uuid_file_0').options.items.push (o)
                form.get ('uuid_file_1').options.items.push (o)
                form.record [fn] = id
                form.refresh ()
            }
            
        })
*/    
    }

    return function (done) {
    
        done (clone ($('body').data ('data')))
        
    }
    
})