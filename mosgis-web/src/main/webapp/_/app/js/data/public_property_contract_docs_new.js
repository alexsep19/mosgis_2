define ([], function () {

    $_DO.update_public_property_contract_docs_new = function (e) {

        var f = w2ui ['public_property_contract_docs_new_form']

        var v = f.values ()

        if (!v ['files']) die (name, 'Укажите, пожалуйста, файл')

        v.files[0].file = new File ([v.files[0].file], v.label, {type: v.files[0].file.type})
        
        var file = get_valid_gis_file (v, 'files')
                                                  
        Base64file.upload (file, {
        
            type: 'public_property_contract_docs',

            data: {
                uuid_ctr: $_REQUEST.id,
                description: v.description,
            },
            
            onprogress: show_popup_progress (file.size),
            
            onloadend: function (id) {
                w2popup.close ()

                var grid = w2ui ['public_property_contract_docs_grid']
                grid.reload (grid.refresh)
            }
            
        })
    
    }

    return function (done) {
    
        var data = clone ($('body').data ('data'))
        
        data.record = $_SESSION.delete ('record')
        
        done (data)
        
    }
    
})