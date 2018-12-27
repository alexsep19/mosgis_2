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

                var r = grid.get (e.recid)
                r ['cnt_' + data.month] = data.workcount
                delete r ['days_bitmask_' + data.month]

                r.cnt = 0
                for (var i = 1; i <= 12; i ++) {
                    var c = r ['cnt_' + i]
                    if (c > 0) r.cnt += c
                }

                grid.set (e.recid, r)

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

            var idx = {}; $.each (d.tb_work_list_items, function () {
                this.cnt = 0
                idx [this.uuid] = this
            })

            $.each (d.cells, function () {

                var r = idx [this.uuid_working_list_item] 

                r.cnt += this.workcount
                r ['cnt_' + this.month] = this.workcount
                r ['days_bitmask_' + this.month] = this.days_bitmask

            })

            done (data)

        })

    }

})