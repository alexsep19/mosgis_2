define ([], function () {

    function get_id () {
        return $('div[name=passport_layout_main_tabs] div.active').parent ().attr ('id').substr (-36)
    }

    $_DO.patch_working_list_common_plan = function (e) {

        var data = {
            uuid_working_list_item: e.recid,
            month:                  e.column - 4, 
            workcount:              parseInt (e.value_new)
        }

        if (data.workcount == parseInt (e.value_previous)) return
        var grid = w2ui [e.target]

        e.done (function () {

            grid.lock ()

            query ({
                type:   'working_plans',
                id:      get_id (),
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
        
        query ({type: 'working_plans', id: get_id ()}, {data: {uuid_working_list: $_REQUEST.id}}, function (d) {
        
            data.plan = d.item
        
            data.records = dia2w2uiRecords (d.tb_work_list_items)
            
            var idx = {}; $.each (d.tb_work_list_items, function () {idx [this.uuid] = this})                       

            $.each (d.cells, function () {

                idx [this.uuid_working_list_item] ['cnt_' + this.month] = this.workcount
                        
            })
        
            done (data)
        
        })
                
    }

})