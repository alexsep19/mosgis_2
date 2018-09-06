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
            
            $('body').data ('data', data)
                
            done (data)
                
        })    

/*    
        query ({type: 'mgmt_contract_objects', part: 'vocs', id: undefined}, {}, function (data) {

            add_vocabularies (data, data)

            query ({type: 'mgmt_contract_objects'}, {}, function (d) {
            
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
*/        

    }

})