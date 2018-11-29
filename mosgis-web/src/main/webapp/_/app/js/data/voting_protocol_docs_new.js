define ([], function () {

    $_DO.update_voting_protocol_docs_new = function (e) {

        var f = w2ui ['voting_protocol_docs_new_form']

        var v = f.values ()

        if (!v ['files']) die (name, 'Укажите, пожалуйста, файл')

        v.files[0].file = new File ([v.files[0].file], v.label, {type: v.files[0].file.type})
        
        var file = get_valid_gis_file (v, 'files')
                                                  
        Base64file.upload (file, {
        
            type: 'voting_protocol_docs',

            data: {
                uuid_protocol: $_REQUEST.id,
                description: v.description,
            },
            
            onprogress: show_popup_progress (file.size),
            
            onloadend: function (id) {
                w2popup.close ()

                var grid = w2ui ['voting_protocol_docs_grid']
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