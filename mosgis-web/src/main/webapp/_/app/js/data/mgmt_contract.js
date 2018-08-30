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
            
                data.item = d.item

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