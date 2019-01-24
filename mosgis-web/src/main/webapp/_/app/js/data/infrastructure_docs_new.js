define ([], function () {

    $_DO.update_infrastructure_docs_new = function (e) {

        var f = w2ui ['infrastructure_docs_new_form']

        var v = f.values ()

        if (!v ['files']) die ('files', 'Укажите, пожалуйста, файл')

        v.files[0].file = new File ([v.files[0].file], v.label, {type: v.files[0].file.type})
        
        var file = get_valid_gis_file (v, 'files')
                                                  
        Base64file.upload (file, {
        
            type: 'infrastructure_docs',

            data: {
                uuid_oki: $_REQUEST.id,
                description: v.description,
                id_type: v.id_type
            },
            
            onprogress: show_popup_progress (file.size),
            
            onloadend: function (id) {
                w2popup.close ()

                var grid = w2ui ['infrastructure_docs_grid']
                grid.reload (grid.refresh)
            }
            
        })
    
    }

    return function (done) {
    
        var data = clone ($('body').data ('data'))
        
        data.record = {id_type: 1}
        
        done (data)
        
    }
    
})