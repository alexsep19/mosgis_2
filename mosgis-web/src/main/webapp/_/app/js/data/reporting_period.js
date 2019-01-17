define ([], function () {

    $_DO.choose_tab_reporting_period = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('reporting_period.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) {
    
        query ({type: 'reporting_periods'}, {}, function (data) {        
        
/*        
            add_vocabularies (data, {
                vc_gis_status: 1,
                vc_actions: 1,
                org_works: 1,
            })
*/        
            
            var it = data.item
            
            it._can = {}               
            
            if (!it.is_deleted) {

                var cach = data.cach

                if (cach 
                    && cach.is_own 
                    && cach ['org.uuid'] == $_USER.uuid_org 
                    && cach.id_ctr_status_gis == 40
                    && it ['tb_work_lists.id_ctr_status'] == 40
                ) {

//                    switch (it.id_ctr_status) {
//                        case 10:
//                        case 11:
                            it._can.edit = 1
//                    }

                }        

            }                                

            $('body').data ('data', data)            

            done (data)
                
        })

    }

})