define ([], function () {

    return function (data, view) {
        
        var $panel
        
        var admin_layout = w2ui ['admin_layout']
        
        if (admin_layout) {            
            admin_layout.unlock ('main')
            $panel = $(admin_layout.el ('main'))
        }
        else {
            $panel = $('body')
            $('title').text ('Реестр адресов OpenData')            
        }
        
        $panel.w2regrid ({ 

            name: 'open_data_grid',             

            show: {
                toolbar: true,
                footer: true,
            },            

            toolbar: {
                items: [                
                    
                    {type: 'menu-radio', id: 'specialSearchButton',
                        text: 'Поиск ошибок',
                        items: [
                            { id: 'DUP',     text: 'Дублирующиеся GUID ФИАС'},
                            { id: 'NO GUID', text: 'Пустые GUID ФИАС'},
                        ]
                    },
                    
                    {type: 'button', id: 'printButton', caption: 'MS Excel', onClick: function () {this.owner.saveAsXLS ()}},
                    {type: 'button', id: 'maxButton', caption: 'Открыть в отдельной вкладке', onClick: function () {openTab ('/open_data')}, off: !admin_layout},
                    {type: 'spacer' },
                    {type: 'button', id: 'historyButton', text: 'История обновления', onClick: function () {use.block ('open_data_log_popup')}},
                
                ].filter (not_off),
                
                onClick: function (e) {
                    
                    if (!/^specialSearchButton:/.test (e.target)) return
                    
                    e.done (function () {
                        this.owner.search ('all', e.item.selected)
                    })

                }                
                
            },                      

            columns: [                
                {field: 'id', caption: 'UNOM',          size: 10, editable: {type: 'text'}},
                {field: 'fiashouseguid', caption: 'GUID ФИАС', size: 30, editable: {type: 'text'}},
                {field: 'address', caption: 'Адрес',    size: 100, editable: {type: 'text'}},
                {field: 'kad_n', caption: 'Кадастровый номер', size: 20, editable: {type: 'text'}},
                {field: 'fn', caption: 'Файл',          size: 5},
                {field: 'line', caption: 'Строка',      size: 5},
                {field: 'is_actual', caption: 'Статус',      size: 15, render: function (r) {return r.is_actual ? 'актуально' : 'не актуально'}},
            ],

            url: '/mosgis/_rest/?type=open_data',
            
            onDblClick: null,

        }).refresh ();

        $('#grid_open_data_grid_search_all').focus ()

    }

})