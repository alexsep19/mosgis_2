define ([], function () {

    function recalcToolbar (e) {e.done (function () {

        var g = w2ui ['house_premises_residental_grid']

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

            name: 'house_premises_residental_grid',
            
            selectType: 'cell',

            multiSelect: false,

            toolbar: {
                items: [
                    {type: 'button', id: 'deleteButton', caption: 'Удалить', onClick: $_DO.delete_house_premises_residental, icon: 'w2ui-icon-cross', disabled: true},
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

            columnGroups : [
                {master: true},
                {master: true},
                {master: true},
                {master: true},
                {span: 2, caption: 'Площадь'},
                {master: true},
                {master: true},
                {master: true},
                {master: true},
                {span: 3, caption: 'Сети'},
                {span: 3, caption: 'Непригодность'},
                {master: true},
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

            columns: [
                {field: "uuid_entrance", caption: "Подъезд", tooltip: "№ подъезда", size: 7, voc: d.entrances},
                {field: "premisesnum", caption: "№", tooltip: "№ помещения", size: 7},
                {field: "cadastralnumber", caption: "Кадастровый №", tooltip: "Кадастровый #", size: 10, editable: {type: 'text'}},
                {field: "code_vc_nsi_30", attr: 'data-mandatory', caption: "Характеристика", tooltip: "Характеристика помещения", size: 10, voc: d.vc_nsi_30, editable: {type: 'list'}},
                
                {field: "totalarea", attr: 'data-mandatory', caption: "Общая, м\xB2", tooltip: "Общая площадь", size: 5, editable: {type: 'float', min: 0}},
                {field: "grossarea", attr: 'data-mandatory', caption: "Жилая, м\xB2", tooltip: "Жилая площадь", size: 5, editable: {type: 'float', min: 0}},

                {field: "f_20002", attr: 'data-mandatory', caption: "К-во комнат", tooltip: "Количество комнат", size: 10, voc: d.vc_nsi_14, editable: {type: 'list'}},
                {field: "f_20125", caption: "К-во проживающих", tooltip: "Количество проживающих", size: 5, editable: {type: 'int'}},
                {field: "f_20061", caption: "Состав семьи", tooltip: "Группы домохозяйств, относительно состава семьи", size: 10, voc: d.vc_nsi_259, editable: {type: 'list'}},
                {field: "f_20059", caption: "Зарегистрировано лиц", tooltip: "Группы домохозяйств, относительно количества зарегистрированных граждан", size: 10, voc: d.vc_nsi_258, editable: {type: 'list'}},

                {field: "f_20054", caption: "Тепло", tooltip: "Направление использования тепловой энергии на нужды отопления", size: 7, voc: d.vc_nsi_254, hidden: true},
                {field: "f_20053", caption: "Газ", tooltip: "Направления использования газа", size: 7, voc: d.vc_nsi_253, hidden: true},
                {field: "f_20056", caption: "Э/оборудование", tooltip: "Наличие электрооборудования", size: 7, voc: d.vc_nsi_261, hidden: true},

                {field: "f_20127", caption: "Основание", size: 10, voc: d.vc_nsi_273, hidden: true},
                {field: "f_20128", caption: "Дата", render: _dt, size: 10, hidden: true},
                {field: "f_20129", caption: "№ док.", size: 10, hidden: true},

                {field: 'terminationdate', caption: 'Дата аннулирования', render: _dt, size: 20, hidden: true},
                {field: 'id_status',  caption: 'ГИС ЖКХ',     size: 10, voc: d.vc_house_status},
            ],
            
            postData: {data: {uuid_house: $_REQUEST.id}},

            url: '/mosgis/_rest/?type=premises_residental',
            
            onDblClick: function (e) {
                if (!this.columns [e.column].editable) openTab ('/premise_residental/' + e.recid)
            },
            
            onAdd:    $_DO.create_house_premises_residental,
            onEdit:   $_DO.edit_house_premises_residental,
            onChange: $_DO.patch_house_premises_residental,

            onSelect: recalcToolbar,
            onUnselect: recalcToolbar,

            onEditField: function (e) {

                var grid     = this
                var record   = grid.get (e.recid)
                if (record.is_annuled) return e.preventDefault ()                

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