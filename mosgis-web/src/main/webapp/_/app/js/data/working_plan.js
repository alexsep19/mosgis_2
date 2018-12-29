define ([], function () {

    $_DO.choose_tab_working_plan = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('working_plan.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) {
    
        query ({type: 'working_plans'}, {}, function (data) {        
        
            add_vocabularies (data, {
                vc_gis_status: 1,
                vc_actions: 1,
                org_works: 1,
            })
        
            var it = data.item
            
            it._can = {cancel: 1}

            if (!it.is_deleted) {

                var cach = data.cach

                if (cach && cach.is_own && cach ['org.uuid'] == $_USER.uuid_org && cach.id_ctr_status_gis == 40) {

                    switch (it.id_ctr_status) {
                        case 10:
                        case 11:
                            it._can.edit = 1
                    }

                    switch (it.id_ctr_status) {                    
                        case 10:
                        case 14:
                            it._can.delete = 1
                    }
                    
                    switch (it.id_ctr_status) {                    
                        case 14:
                        case 120:
                            it._can.alter = 1
                    }

                    switch (it.id_ctr_status) {                    
                        case 40:
                            it._can.edit_plan = 1
                            it._can.unapprove = 1
                    }

                    it._can.update = it._can.edit
                    it._can.approve = it._can.edit

                }        

            }                    
                                
            $('body').data ('data', data)            

            done (data)
                
        })

    }

})