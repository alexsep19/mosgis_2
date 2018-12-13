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

    function is_blocked (it) {
    
        if (it.is_deleted) return true
        
        return false
    
    }
    
    function is_own (data) {
    
        if ($_USER.role.admin) return true
        
        var cach = data.cach
                
        if (cach && cach.is_own) { // есть ОУ — редактировать может сотрудник его организации
        
            return $_USER.uuid_org == data.cach ['org.uuid']
            
        }
        else {                     // нет ОУ — редактировать может ОМСУ соответствующего района
        
            $_USER.role.nsi_20_8 && $_USER.role ['oktmo_' + data.item ['fias.oktmo']]
            
        }
        
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
                
                it._can = {cancel: 1}
                
                if (!is_blocked (it) && is_own (data)) {
                
                    switch (it.id_prtcl_status) {
                        case 10:
                        case 14:
                            it._can.delete = 1
                    }
                    
                    switch (it.id_prtcl_status) {
                        case 10:
                        case 11:
                            it._can.edit = 1
                            it._can.approve = 1
                    }
                    
                    it._can.update = it._can.edit

                    switch (it.id_prtcl_status) {
                        case 14:
                        case 34:
                        case 40:
                            it._can.alter = 1
                    }

                }                

                $('body').data ('data', data)
    
                done (data)

            })

        }) 
        
    }

})