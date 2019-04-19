define ([], function () {

    $_DO.update_overhaul_address_program_doc_files_new = function (e) {

        var f = w2ui ['overhaul_address_program_files_popup_form']

        var v = f.values ()
        
        var file = get_valid_gis_file (v, 'files')
                                                  
        Base64file.upload (file, {
        
            type: 'overhaul_address_program_files',

            data: {
                uuid_oh_addr_pr_doc: $_REQUEST.id,
                description: v.description,
            },
            
            onprogress: show_popup_progress (file.size),

            onloadend: function (id) {
                w2popup.close ()
                var grid = w2ui ['overhaul_address_program_files_grid']
                grid.reload (grid.refresh)
            }

        })
    
    }

    return function (done) {
    
        done (clone ($('body').data ('data')))
        
    }
    
})