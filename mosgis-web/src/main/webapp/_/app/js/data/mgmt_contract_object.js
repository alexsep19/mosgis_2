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
                vc_actions: 1,
            })
            
            var it = data.item

            it._can = {}

            if ($_USER.role.nsi_20_1 && it ['ctr.uuid_org'] == $_USER.uuid_org && !it.is_deleted) {
            
                switch (it ["ctr.id_ctr_status"]) {

                    case 10:
                    case 11:
                        it._can.edit    = 1
                        if (it.id_ctr_status_gis == 40 && !it.is_annuled) it._can.annul = 1
                        break;
                        
                }

                if (!it.contractobjectversionguid) it._can.delete = 1

                it._can.update = it._can.cancel = it._can.edit

            }
            
            $('body').data ('data', data)

            done (data)
                
        })    

    }

})