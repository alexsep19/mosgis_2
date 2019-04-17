define ([], function () {

    $_DO.choose_tab_citizen_compensation = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('citizen_compensation.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) {
    
        query ({type: 'citizen_compensations'}, {}, function (data) {

            var it = data.item
            
            add_vocabularies (data, {
                vc_actions: 1,
                vc_gis_status: 1,
                vc_addr_reg_types: 1,
            })

            it._can = {}

            var is_locked = it.is_deleted

            var is_own = $_USER.role.admin || (it.uuid_org == $_USER.uuid_org)

            if (!is_locked && is_own) {

                switch (it.id_ctr_status) {

                    case 10:
                        it._can.delete = 1
                        it._can.edit = 1           
                        break;
                    case 14:
                        it._can.delete = 1
                        it._can.alter = 1
                        break;
                    case 40:
                    case 34:
                    case 104:
                        it._can.annul = 1
                        break;

                }                    

                it._can.update = it._can.cancel = it._can.edit
            }

            $('body').data('data', data)

            done(data)
        }) 

    }

})