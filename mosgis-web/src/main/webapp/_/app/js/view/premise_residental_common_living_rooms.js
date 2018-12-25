define ([], function () {

    function recalcToolbar (e) {e.done (function () {

        var g = w2ui ['premise_residental_common_living_rooms_grid']

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

        $(w2ui ['premise_residental_layout'].el ('main')).w2regrid ({ 

            name: 'premise_residental_common_living_rooms_grid',
            
            selectType: 'cell',

            multiSelect: false,

            toolbar: {
                items: [
                    {type: 'button', id: 'deleteButton', caption: 'Удалить', onClick: $_DO.delete_premise_residental_common_living_rooms, icon: 'w2ui-icon-cross', disabled: true},
                ]
            },

            show: {
                toolbar: true,
                footer: true,
                toolbarAdd: true,
                toolbarInput: false,
                toolbarSearch: true,
                toolbarReload: false,
            },     

            columnGroups : [
                {master: true},
                {master: true},

                {master: true},

                {master: true},
                {master: true},
                
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
            
                {field: "roomnumber", caption: "№", tooltip: "№ комнаты", size: 7},
                {field: "cadastralnumber", caption: "Кадастровый №", tooltip: "Кадастровый #", size: 10, editable: {type: 'text'}},
                
                {field: "square", attr: 'data-mandatory', caption: "Площадь, м\xB2", tooltip: "Общая площадь", size: 5, editable: {type: 'float', min: 0}},

                {field: "f_20130", attr: 'data-living', caption: "К-во проживающих", tooltip: "Количество проживающих", size: 5, editable: {type: 'int'}},
                {field: "f_20056", caption: "Э/оборудование", tooltip: "Наличие электрооборудования", size: 7, voc: d.vc_nsi_261, hidden: true},

                {field: "f_20132", caption: "Основание", size: 10, voc: d.vc_nsi_273, hidden: true},
                {field: "f_20133", caption: "Дата", render: _dt, size: 10, hidden: true},
                {field: "f_20134", caption: "№ док.", size: 10, hidden: true},

                {field: 'terminationdate', caption: 'Дата аннулирования', render: _dt, size: 20, hidden: true},
                {field: 'id_status', caption: 'ГИС ЖКХ', size: 10},
            ],
            
            postData: {data: {uuid_premise: $_REQUEST.id}},

            url: '/mosgis/_rest/?type=living_rooms',
            
            onDblClick: function (e) {
                if (!this.columns [e.column].editable) openTab ('/living_room/' + e.recid)
            },
            
            onAdd:    $_DO.create_premise_residental_common_living_rooms,
            onChange: $_DO.patch_premise_residental_common_living_rooms,

            onSelect: recalcToolbar,
            onUnselect: recalcToolbar,

            onEditField: function (e) {

                var grid     = this
                var record   = grid.get (e.recid)
                if (record.is_annuled) return e.preventDefault ()

                var col      = grid.columns [e.column]
                
                if (/data-living/.test (col.attr) && record.is_nrs == 1) return e.preventDefault ()
                
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
            
                e.done (function () {
            
                $('tr[recid] td[data-mandatory]').each (function () {
                    var $this = $(this)
                    if ($this.text ()) return
                    if ($this.closest ('tr').text().charAt (0) == 'Н' && $this.is ('[data-living]')) return
                    var p = {title: 'Обязательное поле'}
                    $this.css ({background: '#ffcccc'}).prop (p)
                    $('*', $this).prop (p)
                })

                $('tr[recid]').each (function () {
                    var $this = $(this)
                    if (grid.get ($this.attr ('recid')).is_annuled) $('td', $this).css ({background: '#ccc'})
                })

            })}

        }).refresh ();

    }

})