define ([], function () {

    var b = ['annulButton', 'restoreButton']

    function recalcToolbar (e) {e.done (function () {

        var g = w2ui ['house_passport_lifts_grid']

        var t = g.toolbar

        t.disable (b [0])
        t.disable (b [1])

        if (g.getSelection ().length != 1) return

        t.enable (b [g.get (g.getSelection () [0]).is_annuled])

    })}

    return function (data, view) {
    
        var d = $('body').data ('data')

        var house = JSON.parse (JSON.stringify ($('body').data ('data').item))
        
        if (!house.usedyear) house.usedyear = 1600

        $(w2ui ['passport_layout'].el ('main')).w2regrid ({ 

            name: 'house_passport_lifts_grid',

            multiSelect: false,

            show: {
                toolbar: true,
                footer: true,
                toolbarAdd: true,
                toolbarEdit: true,
                toolbarDelete: true,
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

            toolbar: {
            
                items: [
                    {type: 'button', id: b [0], caption: 'Аннулировать', onClick: $_DO.annul_house_passport_lifts, disabled: true},
                    {type: 'button', id: b [1], caption: 'Восстановить', onClick: $_DO.restore_house_passport_lifts, disabled: true},
                ],
                
            },

            columns: [
                {field: "uuid_entrance", caption: "Подъ.", tooltip: "№ подъезда", size: 7, render: function (r) {return d.entrances [r.uuid_entrance]}},
                {field: "code_vc_nsi_192", caption: "Тип лифта", size: 20, render: function (r) {return d.vc_nsi_192 [r.code_vc_nsi_192]}},
                {field: "factorynum", caption: "Заводской номер", size: 20, editable: {type: 'text'}, attr: 'data-status'},
                
                {field: "f_20007", caption: "Инвентарный номер", size: 20, editable: {type: 'text'}},
                {field: "f_20165", caption: "Грузоподъемность, кг", size: 20, editable: {type: 'float'}},
                {field: "f_20164", caption: "Год ввода в эксплуатацию", size: 20,  editable: {type: 'int', min: house.usedyear, max: (new Date ()).getFullYear (), autoFormat: false}},
                {field: "f_20166", caption: "Нормативный срок службы", size: 20, editable: {type: 'int'}},
                {field: "operatinglimit", caption: "Предельный срок эксплуатации", size: 20, editable: {type: 'int'}},
                {field: "f_20151", caption: "Физический износ, лет", size: 20, editable: {type: 'int'}},
                {field: "f_20124", caption: "Год проведения последнего капитального ремонта", size: 20, editable: {type: 'int', min: house.usedyear, max: (new Date ()).getFullYear (), autoFormat: false}},
                {field: 'terminationdate', caption: 'Дата аннулирования', render: _dt, size: 20, render: _dt},
                {field: "annulmentinfo", caption: "Причина аннулирования. Дополнительная информация", size: 20},
            ],

            postData: {data: {uuid_house: $_REQUEST.id}},

            url: '/mosgis/_rest/?type=lifts',
            
            onDblClick: null,
            
            onAdd: function () {use.block ('lift_new')},
            onEdit: $_DO.edit_house_passport_lifts,
            
            onDelete: $_DO.delete_house_passport_lifts,
            onChange: $_DO.patch_house_passport_lifts,

            onEditField: function (e) {

                var grid     = this
                var record   = grid.get (e.recid)

                if (record.terminationdate) return e.preventDefault ()

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
                    if (!this.factorynum) {
                        if (!this.w2ui) this.w2ui = {}
                        this.w2ui.class = 'status_warning'
                    }
                })
                
                e.done (function () {
                
                    var wording = 'Не заполнено обязательное поле. Запись будет пропущена при отправки дома в ГИС ЖКХ'

                    $('tr.status_warning td[data-status]').attr ('title', wording)
                    $('tr.status_warning td[data-status] div').attr ('title', wording)
                    
                    $('tr[recid]').each (function () {
                        var $this = $(this)
                        if (grid.get ($this.attr ('recid')).is_annuled) $('td', $this).css ({background: '#ccc'})
                    })
                
                })

            },

            onSelect: recalcToolbar,
            
            onUnselect: recalcToolbar,

        }).refresh ();

    }

})