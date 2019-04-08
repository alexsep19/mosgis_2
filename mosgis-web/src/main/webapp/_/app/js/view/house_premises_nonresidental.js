define ([], function () {

    function recalcToolbar (e) {e.done (function () {

        var g = w2ui ['house_premises_nonresidental_grid']

        var t = g.toolbar

        t.disable ('deleteButton')

        if (g.getSelection ().length != 1) return

        var status = g.get (g.getSelection () [0].recid).id_status

        if (status != 20) t.enable ('deleteButton')

    })}

    return function (data, view) {

        var d = clone ($('body').data ('data'))

        var house = d.item

        if (!house.usedyear) house.usedyear = 1600

        $(w2ui ['house_premises_layout'].el ('main')).w2regrid ({ 

            name: 'house_premises_nonresidental_grid',
            
            selectType: 'cell',

            multiSelect: false,

            toolbar: {
                items: [
                    {type: 'button', id: 'deleteButton', caption: 'Удалить', onClick: $_DO.delete_house_premises_nonresidental, icon: 'w2ui-icon-cross', disabled: true},
                ]
            },

            show: {
                toolbar: data.is_premise_editable,
                footer: true,
                toolbarAdd: true,
                toolbarEdit: false,
                toolbarInput: false,                
                toolbarReload: false,
            },    
            
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

            columns: [
                {field: "premisesnum", caption: "№", tooltip: "№ помещения", size: 7},
                {field: "cadastralnumber", caption: "Кадастровый №", tooltip: "Кадастровый #", size: 10, editable: {type: 'text'}},
                
                {field: "totalarea", attr: 'data-mandatory', caption: "Общая, м\xB2", tooltip: "Общая площадь", size: 5, editable: {type: 'float', min: 0}},

                {field: "iscommonproperty", caption: "Общее имущество?", tooltip: "Составляет общее имущество в многоквартирном доме", size: 10, voc: d.bool, editable: {type: 'list'}},
                {field: "f_20003", caption: "Назначение", tooltip: "Назначение помещения, относящегося к общему долевому имуществу собственников помещений", size: 10, voc: d.vc_nsi_17, editable: {type: 'list'}},

                {field: "f_20054", caption: "Тепло", tooltip: "Направление использования тепловой энергии на нужды отопления", size: 7, voc: d.vc_nsi_254, hidden: true},
                {field: "f_20053", caption: "Газ", tooltip: "Направления использования газа", size: 7, voc: d.vc_nsi_253, hidden: true},
                {field: "f_20056", caption: "Э/оборудование", tooltip: "Наличие электрооборудования", size: 7, voc: d.vc_nsi_261, hidden: true},

                {field: 'terminationdate', caption: 'Дата аннулирования', render: _dt, size: 20, hidden: true},
                {field: 'id_status',  caption: 'ГИС ЖКХ',     size: 10, voc: d.vc_house_status},
            ],
            
            postData: {data: {uuid_house: $_REQUEST.id}},

            url: '/_back/?type=premises_nonresidental',
            
            onDblClick: function (e) {
                if (!this.columns [e.column].editable) openTab ('/premise_nonresidental/' + e.recid)
            },
            
            onAdd:    $_DO.create_house_premises_nonresidental,
            onEdit:   $_DO.edit_house_premises_nonresidental,
            onChange: $_DO.patch_house_premises_nonresidental,

            onSelect: recalcToolbar,
            onUnselect: recalcToolbar,

            onEditField: function (e) {

                var grid     = this
                var record   = grid.get (e.recid)
                record.is_blocked = record.is_annuled || !is_own_srca_r(record)
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

            onRefresh: function (e) {e.done (color_data_mandatory)}

        }).refresh ();

    }

})