define ([], function () {

    $_DO.choose_tab_voting_protocol = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('voting_protocol.active_tab', name)
            
        use.block (name)
            
    }            
    
    function is_own (data) {
    
//        if (data.item.is_deleted) return false
        if ($_USER.role.admin)    return true
        
        var cach = data.cach
        
        if (!cach) return false
        if (!cach.is_own) return false
        if (cach ['org.uuid'] != $_USER.uuid_org)
        
        
                    

                
                return ($_USER.role.nsi_20_1 ||
                        $_USER.role.nsi_20_19 ||
                        $_USER.role.nsi_20_20 ||
                        $_USER.role.nsi_20_21 ||
                        $_USER.role.nsi_20_22) &&
                        data.cach.is_own && 
                        $_USER.uuid_org == data.cach['org.uuid']

            }

            return $_USER.role.nsi_20_8 && $_USER.role['oktmo_' + data.item['fias.oktmo']]


        return false
    }    

    return function (done) {        

        query ({type: 'voting_protocols', part: 'vocs', id: undefined}, {}, function (data) {

            if (data.vc_nsi_63) {    
                
                data.vc_nsi_63.forEach ((element, i, arr) => {
                    if (element['id'].indexOf ('.') < 0) element['fake'] = 1
                })

            }

            add_vocabularies (data, data)

            query ({type: 'voting_protocols'}, {}, function (d) {
            
                data.item = d.item

                if (d.cach) data.cach = d.cach
                if (d.owners) {
                    add_vocabularies (d, {owners: {}})
                    data.owners = d.owners
                }
                
                var it = data.item
                
                it._can = {}
                
                

                $('body').data ('data', data)
    
                done (data)

            })

        }) 
        
    }

})