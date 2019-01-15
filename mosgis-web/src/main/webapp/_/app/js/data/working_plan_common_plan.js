define ([], function () {

    $_DO.patch_working_plan_common_plan = function (e) {
    
        if (e.value_new > 99) {
            alert ('Макимальное значение — 99')
            return e.preventDefault ()
        }

        var data = {
            uuid_working_list_item: e.recid,
            month:                  e.column - 4, 
            workcount:              parseInt (e.value_new)
        }

        if (data.workcount == parseInt (e.value_previous)) return
        var grid = w2ui [e.target]

        e.done (function () {

            grid.lock ()

            query ({type: 'working_plans', action: 'update'}, {data: data}, function () {

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

        data.records = dia2w2uiRecords (data.tb_work_list_items)

        var idx = {}; $.each (data.tb_work_list_items, function () {
            this.cnt = 0
            idx [this.uuid] = this
        })

        $.each (data.cells, function () {

            var r = idx [this.uuid_working_list_item] 

            r.cnt += this.workcount
            r ['cnt_' + this.month] = this.workcount
            r ['days_bitmask_' + this.month] = this.days_bitmask

        })

        done (data)

    }

})