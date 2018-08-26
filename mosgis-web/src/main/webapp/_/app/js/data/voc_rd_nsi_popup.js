define ([], function () {

    $_DO.close_voc_rd_nsi_popup = function (e) {
        w2popup.close ()    
    }
        
    $_DO.update_voc_rd_nsi_popup = function (e) {    

        w2popup.lock ('Сохраняем данные...', true)

        var data = {ids: [], code: w2ui ['voc_rd_nsi_popup_form'].values ().code}

        for (var id in $_SESSION.get ('ids')) if (id > 0) data.ids.push (parseInt (id))

        var id = $('body').data ('data').id

        query ({
            type: 'voc_rd_voc', 
            id: id,
            action: 'set_nsi'
        }, {
            data: data
        }, function () {

            w2ui ['rd_cols_grid_' + id].reload ()
            
            w2popup.unlock ()
            
            sessionStorage.removeItem ('is_dirty')
            
        })
        
    }        

    return function (done) {
        
        var data = {}
                
        data.vc_nsi_list = $('body').data ('vc_nsi_list')
        
        var r = {id: $('body').data ('data').nsi.ref}
        
        r.text = data.vc_nsi_list [r.id]
                       
        data.vc_nsi_list.items = [r]
        
        data.nsi_voc = $('body').data ('data').nsi.items

        add_vocabularies (data, {nsi_voc: 1})
        
        data.record = {registrynumber: r}
        
        $_SESSION.set ('ids', {})
        sessionStorage.removeItem ('is_dirty')
                
        done (data)

    }
    
})