define ([], function () {

    var b = ['annulButton', 'restoreButton']

    function recalcToolbar (e) {e.done (function () {

        var g = w2ui ['house_passport_entrances_grid']

        var t = g.toolbar

        t.disable (b [0])
        t.disable (b [1])
        t.disable ('editButton')
        t.disable ('deleteButton')

        if (g.getSelection ().length != 1) return

        var annuled = g.get (g.getSelection () [0]).is_annuled
        var annuled_in_gis = g.get (g.getSelection () [0]).is_annuled_in_gis
        var status = g.get (g.getSelection () [0]).id_status

        t.enable (b [annuled])

        if (annuled) {
            if (!annuled_in_gis) t.disable ('restoreButton')
            t.disable ('editButton')
            t.disable ('deleteButton')
        }
        else {
            t.enable ('editButton')
            t.enable ('deleteButton')
        }

        if (status == 10) t.disable ('annulButton')
        else if (status == 20) t.disable ('deleteButton')

    })}

    return function (data, view) {    
    
        var d = $('body').data ('data')

        var house = JSON.parse (JSON.stringify (d.item))
        
        if (!house.usedyear) house.usedyear = 1600        
                
        if (!house.minfloorcount) house.minfloorcount = house.floorcount
        
        if (!house.floorcount) house.floorcount = 99
        if (!house.minfloorcount) house.minfloorcount = 1
        
        var columns = [
            {field: 'entrancenum', caption: 'Номер',    size: 80},
            {field: 'storeyscount', caption: 'Этажность',  size: 20,  editable: {type: 'int', min: house.minfloorcount, max: house.floorcount}},
            {field: 'creationyear', caption: 'Год постройки',    size: 20,  editable: {type: 'int', min: house.usedyear, max: (new Date ()).getFullYear (), autoFormat: false}},
            {field: 'terminationdate', caption: 'Дата аннулирования', render: _dt,    size: 20},
            {field: 'code_vc_nsi_330', caption: 'Причина аннулирования', size: 20, voc: d.vc_nsi_330, hidden: 1},
            {field: 'annulmentinfo', caption: 'Дополнительно', size: 20, hidden: 1}
        ]
        
        $.each (d.vc_nsi_192.items, function () {
        
            columns.push ({
                field: 'cnt_' + this.id,
                caption: this.label,
                size: 20,
                editable: {type: 'int', min: 0, max: 12},
                _is_lift: true,
            })
            
        })
        
        columns.push({field: 'id_status',  caption: 'ГИС ЖКХ',     size: 10, voc: d.vc_house_status})

        $(w2ui ['passport_layout'].el ('main')).w2regrid ({ 

            name: 'house_passport_entrances_grid',

            multiSelect: false,

            show: {
                toolbar: data.is_passport_editable || data.is_own_srca,
                footer: true,
                toolbarAdd: true,
                toolbarInput: false,
                toolbarReload: false,
            },     
            
            toolbar: {
            
                items: [
                    {type: 'button', id: 'deleteButton', caption: 'Удалить', onClick: $_DO.delete_house_passport_entrances, icon: 'w2ui-icon-cross', disabled: true},
                    {type: 'button', id: 'editButton', caption: 'Изменить', onClick: $_DO.edit_house_passport_entrances, icon: 'w2ui-icon-pencil', disabled: true},
                    {type: 'button', id: b [0], caption: 'Аннулировать', onClick: $_DO.annul_house_passport_entrances, disabled: true},
                    {type: 'button', id: b [1], caption: 'Восстановить', onClick: $_DO.restore_house_passport_entrances, disabled: true},
                ],
                
            },                        

            columnGroups : [
                {master: true},
                {master: true},
                {master: true},
                {span: 3, caption: 'Аннулирование'},
                {span: d.vc_nsi_192.items.length, caption: 'Количество лифтов'},
                {master: true},
            ],            

            searches: [            
                {field: 'is_annuled', caption: 'Актуальность', type: 'enum', options: {items: [
                    {id: 0, text: "Актуальные"},
                    {id: 1, text: "Аннулированные"},
                ]}},
            ],
            
            last: {logic: 'AND'},

            searchData: [
                {
                    field:    "is_annuled",
                    operator: "in",
                    type:     "enum",
                    value:    [{id: 0, text: "Актуальные"}],
                }
            ],

            columns: columns,

            postData: {data: {uuid_house: $_REQUEST.id}},

            url: '/_back/?type=entrances',

            onDblClick: null,

            onAdd: function () {use.block ('entrance_new')},
            onChange: $_DO.patch_house_passport_entrances,

            onEditField: function (e) {

                var grid     = this
                var record   = grid.get (e.recid)
                record.is_blocked = record.terminationdate || !is_own_srca_r(record)
                if (record.is_blocked) return e.preventDefault ()

                var col      = grid.columns [e.column]
                var editable = col.editable
                var v        = record [col.field]

                if (editable.type == 'date') {
                    e.value = v ? new Date (v.substr (0, 10)) : new Date ()
                }
                else {
                    e.value = v
                }
                
            },

            onRefresh: function (e) {

                var grid = this

                $.each (grid.records, function () {
                    if (this.w2ui) delete this.w2ui.changes
                })

                e.done (color_data_mandatory)

            },
            
            onSelect: recalcToolbar,
            
            onUnselect: recalcToolbar,
            
        }).refresh ();

    }

})