define ([], function () {

    $_DO.choose_tab_working_list = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('working_list.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) {
    
        query ({type: 'working_lists'}, {}, function (data) {        
        
            add_vocabularies (data, {
                vc_gis_status: 1,
                vc_actions: 1,
            })
        
            var it = data.item
            
            it.dt_from = it.dt_from.substr (0, 10)
            it.dt_to   = it.dt_to.substr (0, 10)
        
            var dt = new Date ((it ['cao.startdate'] || it ['cho.startdate']) + 'Z')

            function dtIso      () {return dt.toISOString ().substr (0, 10)}
            function dtIncMonth () {dt.setMonth (dt.getMonth () + 1)}

            var ms_to = new Date ((it ['cao.enddate'] || it ['cho.enddate']) + 'Z').getTime ()

            dt.setDate (1)

            data.periods = []

            while (dt.getTime () <= ms_to) {
            
                dtIncMonth ()
                dt.setDate (0)
                
                data.periods.push ({
                    id: dtIso (),
                    text: w2utils.settings.fullmonths [dt.getMonth ()] + ' ' + dt.getFullYear (),
                })

                dt.setDate (1)
                dtIncMonth ()

                if (dt.getTime () > ms_to) break

            }
                    
            $('body').data ('data', data)            

            done (data)
                
        })

    }

})