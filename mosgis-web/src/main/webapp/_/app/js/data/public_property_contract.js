define ([], function () {

    $_DO.choose_tab_public_property_contract = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('public_property_contract.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) {        
    
            query ({type: 'public_property_contracts'}, {}, function (data) {
            
                add_vocabularies (data, {
                    vc_actions: 1,
                    vc_gis_status: 1,
                    vc_voting_eligibility: 1,
                    vc_voting_forms: 1,
                    vc_voting_types: 1,
                    vc_pp_ctr_file_types: 1
                })
                
                var it = data.item
                
                it.last_annul = data.last_annul

                it._can = {}
                
                var is_locked = it.is_deleted || (it.uuid_org != $_USER.uuid_org)
                                
                if (!is_locked) {

                    switch (it.id_ctr_status) {
                        case 10:
                        case 14:
                            it._can.delete = 1
                    }
                    
                    switch (it.id_ctr_status) {
                        case 10:
                        case 11:
                            it._can.edit = 1
                            it._can.approve = 1
                    }
                    
                    it._can.update = it._can.edit

                    switch (it.id_ctr_status) {
                        case 14:
                        case 34:
                        case 40:
                            it._can.alter = 1
                    }
                    
                    switch (it.id_ctr_status) {
                        case 40:
                            it._can.annul = 1
                            it._can.create_payment = 1
                    }
                    
                }

                $('body').data ('data', data)

                done (data)

            })


    }

})