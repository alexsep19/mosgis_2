define ([], function () {

    $_DO.choose_tab_mgmt_contract_payment = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('mgmt_contract_payment.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) {
    
        query ({type: 'contract_payments'}, {}, function (data) {

            add_vocabularies (data, {
                vc_ctr_pay_types: 1,
                vc_actions: 1,
                org_works: 1,
            })

            var it = data.item

            it._can = {}

            var is_locked = it.is_deleted
            
            if (!is_locked) switch (it.id_ctr_status_gis) {
                case 20:
                case 70:
                case 110:
                    is_locked = true
            }

            var is_own = $_USER.role.admin || ($_USER.role.nsi_20_1 && it ['ctr.uuid_org'] == $_USER.uuid_org)

            if (!is_locked && is_own) {
            
                switch (it ["ctr.id_ctr_status"]) {

                    case 10:
                    case 11:
                        it._can.edit    = 1
//                        if (it.id_ctr_status_gis == 40 && !it.is_annuled) it._can.annul = 1
                        break;
                        
                }

                it._can.delete = it._can.update = it._can.cancel = it._can.edit

            }

            $('body').data ('data', data)

            done (data)
                
        })    

    }

})