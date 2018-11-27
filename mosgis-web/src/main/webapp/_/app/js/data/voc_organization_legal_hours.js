define ([], function () {

    $_DO.patch_voc_organization_legal_hours = function (e) {

        var grid = this

        var col = grid.columns [e.column]

        var d = {
            weekday: '' + e.recid,
            k: col.field,
            v: normalizeValue(e.value_new, col.editable.type)
        }

        if (col.editable.type == 'time' && d.v.length < 5) {
            d.v = '0' + d.v
        }

        if (d.v != null)
            d.v = String(d.v)

        var r = grid.get(e.recid)

        var blocks = ['open', 'break', 'reception']

        for (var idx = 0; idx < blocks.length; idx++) {

            var i  = blocks [idx]

            var open_from = i + '_from'
            var open_to = i + '_to'

            if (r[open_to] && d.k == open_from && d.v > r[open_to]) {
                return grid_hours_edit_failed('Укажите окончание периода позже начала периода', e, grid)
            }

            if (r[open_from] && d.k == open_to && r[open_from] > d.v) {
                return grid_hours_edit_failed('Укажите окончание периода позже начала периода', e, grid)
            }
        }

        grid.lock()

        var tia = {type: 'voc_organizations', action: 'patch_hours'}

        query(tia, {data: d}, function () {

            query({type: 'voc_organizations', part: 'hours'}, {}, function (data) {

                $.each(grid.records, function () {

                    if (this.weekday == e.recid) {
                        this [d.k] = d.v
                    }

                    delete this.w2ui
                })

                grid.unlock ()

                grid.refresh()

            })

        }, edit_failed(grid, e))
    }

    function grid_hours_edit_failed (msg, e, grid) {

        e.stopPropagation();

        e.preventDefault()

        grid.editField(e.recid, e.column, e.value_original)

        alert(msg)

        return false
    }

    function fill_voc_organization_legal_hours(records) {

        var day2hours = {}

        $.each(records, function (idx, i) {
            day2hours [i.weekday] = i
        })

        var rows = []

        $.each (w2utils.settings.fulldays, function(idx, label){

            var i = day2hours [idx] || {
                weekday        : idx,
                open_from      : null,
                open_to        : null,
                break_from     : null,
                break_to       : null,
                reception_from : null,
                reception_to   : null,
                is_holiday     : 0,
                note           : null
            }

            i.recid = i.id = i.weekday
            i.label = label

            rows.push (i)
        })

        return rows
    }

    return function (done) {

        var layout = w2ui ['voc_organization_legal_layout']

        if (layout)
            layout.unlock('main')

        query ({type: 'voc_organizations', part: 'hours'}, {}, function (data) {

            data.records = dia2w2uiRecords(fill_voc_organization_legal_hours(data.voc_organization_hours))

            done (clone(data));

        })
    }

})