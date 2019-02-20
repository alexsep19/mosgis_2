define ([], function () {

    $_DO.update_metering_device_doc_new = function (e) {

        var f = w2ui ['metering_device_common_service_payments_popup_form']

        var v = f.values ()
        
        var file = get_valid_gis_file (v, 'files')
                                                  
        Base64file.upload (file, {
        
            type: 'metering_device_docs',

            data: {
                uuid_meter: $_REQUEST.id,
                description: v.description,
                id_type: v.id_type,
            },
            
            onprogress: show_popup_progress (file.size),

            onloadend: function (id) {
                w2popup.close ()
                var grid = w2ui ['metering_device_docs_grid']
                grid.reload (grid.refresh)
            }

        })
    
    }

    return function (done) {
    
        done (clone ($('body').data ('data')))
        
    }
    
})