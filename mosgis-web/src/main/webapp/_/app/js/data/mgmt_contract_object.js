define ([], function () {

    $_DO.choose_tab_mgmt_contract_object = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('mgmt_contract_object.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) {
    
        query ({type: 'contract_objects'}, {}, function (data) {
        
            add_vocabularies (data, {
                vc_contract_doc_types: 1,
                vc_nsi_3: 1,
                tb_add_services: 1,
                vc_gis_status: 1,
            })

            $('body').data ('data', data)

            done (data)
                
        })    

    }

})