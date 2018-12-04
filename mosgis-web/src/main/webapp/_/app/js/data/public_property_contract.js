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
                    voc_gis_status: 1
                })            

                var it = data.item
                
                it.last_approve = data.last_approve
                
/*                
                var term = d.last_termination
                if (term) {
                    term.reason = data.vc_nsi_54 [term.code_vc_nsi_54]
                    term.file = d.termination_file
                    it.last_termination = term
                }                
*/                

                it._can = {}
/*
                if ($_USER.role.nsi_20_1 && !it.is_deleted) {

                    switch (it.id_ctr_status) {

                        case 10:
                            it._can.delete  = 1
                        case 11:
                            it._can.edit    = 1
                            it._can.approve = 1
                            break;
                        case 40:
                            it._can.terminate = 1
                        case 14:
                        case 34:
                        case 90:
                            it._can.alter   = 1
                            break;

                    }

                    switch (it.id_ctr_status) {
                        case 40:
                        case 94:
                        case 100:
                            it._can.annul   = 1
                            break;
                    }
                    
                    if (it.id_ctr_state_gis == 50) {
                    
                        switch (it.id_ctr_status) {
                            case 40:
                                it._can.rollover = 1
                                break;
                        }
                        
                    }
                    
                    switch (it.id_ctr_status) {
                        case 10: // Project
                        case 14: // _failed_placing
                            break;
                        default:
                            switch ((0 + it.id_ctr_status) % 10) {
                                case 2: // _pending_rq_...
                                case 3: // _pending_rp_...
                                    break;
                                default:
                                    it._can.refresh = 1
                                    it._can.reload = 1
                            }
                    }

                    it._can.update = it._can.cancel = it._can.edit

                }
*/                

                $('body').data ('data', data)

                done (data)

            })


    }

})