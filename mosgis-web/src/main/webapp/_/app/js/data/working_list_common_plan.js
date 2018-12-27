define ([], function () {

    $_DO.patch_working_list_common_plan = function (e) {

        var data = {
            month:     e.column - 4, 
            workcount: parseInt (e.value_new)
        }

        if (data.workcount == parseInt (e.value_previous)) return
        var grid = w2ui [e.target]

        e.done (function () {

            grid.lock ()

            query ({
                type:   'working_plans',
                id:      $('div[name=passport_layout_main_tabs] div.active').parent ().attr ('id').substr (-36),
                action: 'update',
            }, 

            {data: data},

            function () {
                grid.unlock ()
                grid.refresh ()
            })

        }) 

    }

    return function (done) {        

        var layout = w2ui ['passport_layout']

        if (layout) layout.unlock ('main')
        
        var data = clone ($('body').data ('data'))
        
        query ({type: 'working_list_items', id: undefined}, {data: {uuid_working_list: $_REQUEST.id}}, function (d) {
        
            data.records = dia2w2uiRecords (d.tb_work_list_items)
        
            done (data)
        
        })
                
    }

})