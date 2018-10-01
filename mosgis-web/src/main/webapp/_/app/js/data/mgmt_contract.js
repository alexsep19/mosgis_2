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
                            
                var it = data.item = d.item

                it._can = {}

                if ($_USER.role.nsi_20_1 && !it.is_deleted) {

                    switch (it.id_ctr_status) {

                        case 10:
                            it._can.delete  = 1
                        case 11:
                            it._can.edit    = 1
                            it._can.approve = 1
                            break;
                        case 14:
                        case 34:
                        case 40:
                        case 90:
                        case 94:
                            it._can.alter   = 1
                            break;

                    }

                    if ((0 + it.id_ctr_status) % 10 == 0 && it.id_ctr_status > 10) {
                        it._can.refresh = 1
                    }

                    it._can.update = it._can.cancel = it._can.edit

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