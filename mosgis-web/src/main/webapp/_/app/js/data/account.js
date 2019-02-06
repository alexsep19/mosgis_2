define ([], function () {

    $_DO.choose_tab_account = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('account.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) {

        query ({type: 'accounts'}, {}, function (data) {        

            add_vocabularies (data, {
                vc_acc_types: 1,
                vc_gis_status: 1,
                vc_actions: 1,
            })
            
            var it = data.item
            
            it._can = {cancel: 1}

            if (!it.is_deleted && it.uuid_org == $_USER.uuid_org) {

                switch (it ['ca.id_ctr_status'] || it ['ch.id_ctr_status']) {

                        case 40:
                        case 42:
                        case 43:
                        case 34:
                        case 11:
                        case 92:
                        case 93:
                        case 94:
                        case 100:
                            it._can.edit = 1

                }            

                if (it._can.edit) {

                    it._can.update = 1

                    switch (it.id_ctr_status) {
                        case 10:
                        case 14:
                            it._can.delete = 1
                    }

                }

            }            
                        
            $('body').data ('data', data)
    
            done (data)

        }) 
        
    }

})