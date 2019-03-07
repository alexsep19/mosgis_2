define ([], function () {

    $_DO.choose_tab_mgmt_contract = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('mgmt_contract.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) {        

        query ({type: 'mgmt_contracts', part: 'vocs', id: undefined}, {}, function (data) {

            add_vocabularies (data, data)

            query ({type: 'mgmt_contracts'}, {}, function (d) {
            
                data.tb_bnk_accts = d.tb_bnk_accts
                
                var it = data.item = d.item

                data.bnk_accts_actual = data.tb_bnk_accts.map (function (i) {return {
                    id: i.uuid,
                    text: 'Р/сч. ' + i.accountnumber + ', ' + i ['bank.namep'] +
                        (i.uuid_org == it.uuid_org ? '' : ', владелец: ' + i ['org.label'])
                }})

                data.bnk_accts_set = !it.uuid_bnk_acct ? [] : [{
                    id: it.uuid_bnk_acct,
                    text: 'Р/сч. ' + it ['bank_acct.accountnumber'] + ', ' + it ['vc_bic.namep'] +
                        (it ['bank_acct.uuid_org'] == it.uuid_org ? '' : ', владелец: ' + it ['org_bank_acct.label'])
                }]              
                
                it.last_approve = d.last_approve
                
                var term = d.last_termination
                if (term) {
                    term.reason = data.vc_nsi_54 [term.code_vc_nsi_54]
                    term.file = d.termination_file
                    it.last_termination = term
                }                

                it._can = {}

                if ($_USER.role.nsi_20_1 && !it.is_deleted) {

                    switch (it.id_ctr_status) {

                        case 10:
                            it._can.delete  = 1
                        case 11:
                            it._can.edit    = 1
                            it._can.approve = 1
                            break;
                        case 40:
                        case 94:
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
                    
                    switch (it.id_ctr_status) {
                        case 11:
                        case 34:
                        case 40:
                        case 42:
                        case 43:                        
                        case 92:
                        case 93:
                        case 94:
                        case 100:
                            it._can.create_account   = 1
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
                    
                    it._can.set_bank_acct = it.id_ctr_status > 11

                }

                $('body').data ('data', data)
                
                if (d.item.uuid_org_customer) {
                
                    query ({type: 'voc_organizations', id: d.item.uuid_org_customer, part: 'mgmt_nsi_58'}, {}, function (dd) {

                        add_vocabularies (dd, dd)

                        data.vc_nsi_58 = dd.vc_nsi_58

                        done (data)

                    })

                }
                else {

                    data.vc_nsi_58.items = data.vc_nsi_58.items.filter (function (i) {return 'it.isdefault' in i})

                    done (data)

                }

            })

        })

    }

})